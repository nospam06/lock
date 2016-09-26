package com.ohmyapp.lock.impl.client;

import com.ohmyapp.lock.LockException;
import com.ohmyapp.lock.api.Client;
import com.ohmyapp.lock.api.ServerProvider;
import com.ohmyapp.lock.pojo.LockRequest;
import com.ohmyapp.lock.pojo.LockResponse;
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
import java.util.Map;
import java.util.UUID;

/**
 * Created by Emerald on 9/15/2016.
 * lock client
 */
public class LockClient implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockClient.class);
    private static final ServerProvider PROVIDER = SystemContext.getProvider();
    private final String id = UUID.randomUUID().toString();
    private final Map<String, SocketConnection> connectedServers = new HashMap<>();

    @Override
    public boolean onStart() throws LockException {
        return true;
    }

    @Override
    public boolean lock(String objectName, String key) {
        String server = PROVIDER.returnServer(objectName);
        int port = PROVIDER.returnClientPort(objectName);
        try {
            SocketConnection socketConnection = connect(server, port);
            LockResponse lockResponse = sendRequest(LockRequest.LOCK, objectName, key, socketConnection);
            return lockResponse.isSuccess();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            connectedServers.remove(server);
        }
        return false;
    }

    @Override
    public boolean release(String objectName, String key) {
        String server = PROVIDER.returnServer(objectName);
        int port = PROVIDER.returnClientPort(objectName);
        try {
            SocketConnection socketConnection = connect(server, port);
            LockResponse lockResponse = sendRequest(LockRequest.RELEASE, objectName, key, socketConnection);
            return lockResponse.isSuccess();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            connectedServers.remove(server);
        }
        return false;
    }

    private LockResponse sendRequest(String action, String objectName, String key, SocketConnection socketConnection)
            throws IOException {
        LOGGER.debug(id + " - attempting to {} {} {}", action, objectName, key);
        LockRequest lockRequest = new LockRequest();
        lockRequest.setAction(action);
        lockRequest.setRequesterId(id);
        lockRequest.setObjectName(objectName);
        lockRequest.setKey(key);
        String lockString = SystemContext.GSON.toJson(lockRequest);

        PrintWriter outWriter = socketConnection.getWriter();
        outWriter.println(lockString);
        outWriter.flush();
        BufferedReader inReader = socketConnection.getReader();
        String response = inReader.readLine();
        LockResponse lockResponse = SystemContext.GSON.fromJson(response, LockResponse.class);
        if (lockResponse.isSuccess()) {
            LOGGER.debug(id + " - {} {} {} successful", action, objectName, key);
        } else {
            LOGGER.debug(id + " - {} {} {} unsuccessful", action, objectName, key);
        }
        return lockResponse;
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void onStop() {
        for (Map.Entry<String, SocketConnection> entry : connectedServers.entrySet()) {
            onStop(entry.getKey(), entry.getValue().getSocket());
        }
        connectedServers.clear();
    }

    private void onStop(String hash, Socket client) {
        if (client != null && client.isConnected()) {
            try {
                client.close();
                LOGGER.debug(id + " - disconnected from lock server {}", hash);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private SocketConnection connect(String server, int port) throws IOException {
        SocketConnection socketConnection = connectedServers.get(server);
        if (socketConnection != null) {
            return socketConnection;
        }
        LOGGER.debug(id + " - connecting to lock server {} port {}", server, port);
        Socket client = new Socket(server, port);
        LOGGER.debug(id + " - connected to lock server {} port {}", server, port);
        PrintWriter outWriter = LockUtils.createWriter(client);
        BufferedReader inReader = LockUtils.createReader(client);
        socketConnection = new SocketConnection(client, outWriter, inReader);
        connectedServers.put(server, socketConnection);
        return socketConnection;
    }


}
