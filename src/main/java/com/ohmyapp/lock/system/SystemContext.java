package com.ohmyapp.lock.system;

import com.google.gson.Gson;
import com.ohmyapp.lock.api.Publisher;
import com.ohmyapp.lock.api.Server;
import com.ohmyapp.lock.api.ServerProvider;
import com.ohmyapp.lock.impl.messaging.LockPublisher;
import com.ohmyapp.lock.impl.messaging.LockSubscriber;
import com.ohmyapp.lock.impl.provider.ClasspathServerProvider;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Emerald on 9/18/2016.
 * system context
 */
public class SystemContext {
    public static final String STOP = "stopReplicating";
    public static final Gson GSON = new Gson();

    private static final String JVM_SERVER_PORT = System.getProperty("com.ohmyapp.lock.server.port");
    private static final int SERVER_PORT = JVM_SERVER_PORT == null ? 20000 : Integer.parseInt(JVM_SERVER_PORT);
    private static final Publisher publisher = new LockPublisher();
    private static final LockSubscriber lockSubscriber = new LockSubscriber();
    private static final BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(100, true);

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static ServerProvider provider = new ClasspathServerProvider();

    /**
     * @return server port
     */
    public static int getServerPort() {
        return SERVER_PORT;
    }

    /**
     * @return ExecutorService
     */
    public static ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * @param inExecutorService ExecutorService
     */
    static void setExecutorService(ExecutorService inExecutorService) {
        executorService = inExecutorService;
    }

    /**
     * @return Provider
     */
    public static ServerProvider getProvider() {
        return provider;
    }

    /**
     * @param inProvider Provider
     */
    static void setProvider(ServerProvider inProvider) {
        provider = inProvider;
    }

    /**
     * @return Publisher
     */
    public static Publisher getPublisher() {
        return publisher;
    }

    /**
     * @return Subscriber
     */
    public static Server getSubscriber() {
        return lockSubscriber;
    }

    /**
     * @return blocking queue
     */
    public static BlockingQueue<String> getQueue() {
        return blockingQueue;
    }
}
