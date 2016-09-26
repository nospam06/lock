package com.ohmyapp.lock.impl.server;

import com.ohmyapp.lock.LockException;
import com.ohmyapp.lock.api.Server;
import com.ohmyapp.lock.api.ServerProvider;
import com.ohmyapp.lock.pojo.LockRequest;
import com.ohmyapp.lock.system.SystemContext;
import com.ohmyapp.lock.utils.LockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Lock Server
 */
public class LockServer implements Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockServer.class);
    private static final ServerProvider PROVIDER = SystemContext.getProvider();
    private Map<String, Server> workerMap = new HashMap<>();
    private ConcurrentMap<String, ConcurrentMap<String, String>> lockMap = new ConcurrentHashMap<>();
    private volatile boolean start = false;

    @Override
    public boolean onStart() throws LockException {
        return PROVIDER.isPrimary() || PROVIDER.isSecondary();
    }

    @Override
    public void onRun() {
        do {
            try {
                if (PROVIDER.isPrimary()) {
                    onLeading();
                }
                if (PROVIDER.isSecondary()) {
                    onFollowing();
                }
                if (start) {
                    PROVIDER.init();
                }
            } catch (LockException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } while (start);
    }

    /**
     * leader
     * serve client
     * create publisher to publish changes to followers
     */
    private void onLeading() {
        start = true;
        ExecutorService executorService = SystemContext.getExecutorService();
        Server subscriber = SystemContext.getSubscriber();
        LockServerCallable lockSubscriberCallable = new LockServerCallable(subscriber);
        Future<Boolean> future2 = executorService.submit(lockSubscriberCallable);
        LOGGER.debug("Subscriber submitted {}", future2);
        int serverPort = PROVIDER.returnServerPort();
        LOGGER.debug("starting server on {} port {}", PROVIDER.getLocalhostName(), serverPort);
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            serverSocket.setReuseAddress(true);
            while (start) {
                Socket socket = serverSocket.accept();
                LockWorker lockWorker = new LockWorker(socket, lockMap);
                LockServerCallable workerCallable = new LockServerCallable(lockWorker);
                Future<Boolean> future = executorService.submit(workerCallable);
                LOGGER.debug("Worker submitted {}", future);
                workerMap.put(lockWorker.getId(), lockWorker);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * create subscribers
     * receive replica from leader
     */
    private void onFollowing() throws LockException {
        start = true;
        syncWithLeader();
        receiveReplica();
    }

    @SuppressWarnings("unchecked")
    private void syncWithLeader() throws LockException {
        String server = PROVIDER.requestServerList().get(PROVIDER.getCurrentLeader());
        int serverPort = PROVIDER.returnServerPort();
        try (Socket socket = new Socket(server, serverPort)) {
            LockRequest request = new LockRequest();
            request.setAction(LockRequest.SYNC);
            request.setRequesterId(PROVIDER.getLocalhostName());
            String requestString = SystemContext.GSON.toJson(request);
            PrintWriter writer = LockUtils.createWriter(socket);
            writer.println(requestString);
            writer.flush();
            LOGGER.debug("sync request sent to {} {}", server, serverPort);
            BufferedReader reader = LockUtils.createReader(socket);
            String respond = reader.readLine();
            LOGGER.debug("sync respond received {}", respond);
            lockMap = SystemContext.GSON.fromJson(respond, lockMap.getClass());
            writer.close();
            reader.close();
            socket.close();
        } catch (IOException ioe) {
            LOGGER.error(ioe.getMessage(), ioe);
        }
    }

    private void receiveReplica() {
        int replicaPort = PROVIDER.returnSecondaryPort();
        try (ServerSocket replicaSocket = new ServerSocket(replicaPort)) {
            replicaSocket.setReuseAddress(true);
            LOGGER.debug("Ready to receive change from leader on replicaPort {}", replicaPort);
            Socket socket = replicaSocket.accept();
            LockWorker lockWorker = new LockWorker(socket, lockMap);
            boolean start = lockWorker.onStart();
            if (start) {
                lockWorker.onRun();
            }
            lockWorker.onStop();
        } catch (IOException | LockException ioe) {
            LOGGER.error(ioe.getMessage(), ioe);
        }
    }

    @Override
    public void onStop() {
        start = false;
        for (Server lockWorker : workerMap.values()) {
            lockWorker.onStop();
        }
        if (PROVIDER.isPrimary()) {
            LOGGER.info("Shutting down lock server");
            Server subscriber = SystemContext.getSubscriber();
            subscriber.onStop();
        }
    }

    @Override
    public boolean isStarted() {
        return start;
    }
}