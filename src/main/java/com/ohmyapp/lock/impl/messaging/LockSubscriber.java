package com.ohmyapp.lock.impl.messaging;

import com.ohmyapp.lock.LockException;
import com.ohmyapp.lock.api.Server;
import com.ohmyapp.lock.api.ServerProvider;
import com.ohmyapp.lock.pojo.LockRequest;
import com.ohmyapp.lock.pojo.SocketConnection;
import com.ohmyapp.lock.system.SystemContext;
import com.ohmyapp.lock.utils.LockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Emerald on 9/18/2016.
 * Subscribe to changes
 */
public class LockSubscriber implements Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockSubscriber.class);
    private final Map<String, SocketConnection> connectedServers = new HashMap<>();

    @Override
    public void onRun() {
        LockRequest request = new LockRequest();
        request.setAction(LockRequest.PING);
        String ping = SystemContext.GSON.toJson(request);
        sendMessageToFollowers(ping);

        BlockingQueue<String> queue = SystemContext.getQueue();
        try {
            while (true) {
                LOGGER.debug("waiting for change to sync");
                String message = queue.poll(60, TimeUnit.SECONDS);
                if (SystemContext.STOP.equals(message)) {
                    break;
                }
                if (message == null || message.isEmpty()) {
                    message = ping;
                }
                sendMessageToFollowers(message);
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void sendMessageToFollowers(String message) {
        ServerProvider provider = SystemContext.getProvider();
        if (!provider.isPrimary()) {
            // only primary node send message
            return;
        }
        try {
            List<String> serverList = provider.requestServerList();
            LOGGER.debug("Sending change to replica - {}", message);
            int i = 0;
            for (String server : serverList) {
                int port = provider.returnSecondaryPortOffset() + i++;
                if (server.equals(provider.getLocalhostName())) {
                    continue;
                }
                LOGGER.debug("attempt to send {} to replica server {} on port {}", message, server, port);
                SocketConnection connection = connectedServers.get(server);
                try {
                    if (connection == null || connection.getSocket().isClosed()) {
                        LOGGER.debug("creating new connection to replica server {} on port {}", server, port);
                        Socket socket = new Socket(server, port);
                        PrintWriter writer = LockUtils.createWriter(socket);
                        BufferedReader reader = LockUtils.createReader(socket);
                        connection = new SocketConnection(socket, writer, reader);
                        connectedServers.put(server, connection);
                    }
                    PrintWriter writer = connection.getWriter();
                    writer.println(message);
                    writer.flush();
                    BufferedReader reader = connection.getReader();
                    reader.readLine();
                    LOGGER.debug("{} sent to replica server {} on port {}", message, server, port);
                } catch (IOException ioe) {
                    LOGGER.error("cannot send changes to replica server " + server, ioe);
                    connectedServers.remove(server);
                }
            }
        } catch (LockException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean onStart() throws LockException {
        return true;
    }

    @Override
    public void onStop() {
        BlockingQueue<String> queue = SystemContext.getQueue();
        boolean offer = queue.offer(SystemContext.STOP);
        LOGGER.debug("Stop request " + offer);
    }

    @Override
    public boolean isStarted() {
        return true;
    }
}
