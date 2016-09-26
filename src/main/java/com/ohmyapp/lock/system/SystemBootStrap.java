package com.ohmyapp.lock.system;

import com.ohmyapp.lock.LockException;
import com.ohmyapp.lock.api.LifeCycle;
import com.ohmyapp.lock.api.ServerProvider;
import com.ohmyapp.lock.impl.server.LockServer;
import com.ohmyapp.lock.impl.server.LockServerCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Lock Server
 */
public class SystemBootStrap implements LifeCycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemBootStrap.class);
    private LockServer lockServer;
    private ExecutorService executorService;

    @Override
    public boolean onStart() throws LockException {
        if (isStarted()) {
            throw new LockException("System already started", null);
        }
        String serverProvider = System.getProperty("com.ohmyapp.lock.server.provider");
        if (serverProvider != null && !serverProvider.isEmpty()) {
            try {
                Object provider = Class.forName(serverProvider).newInstance();
                SystemContext.setProvider((ServerProvider) provider);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOGGER.error("cannot instantiate server provider. use default classpath provider", e);
            }
        }
        boolean server = SystemContext.getProvider().init();
        if (server) {
            executorService = Executors.newCachedThreadPool();
            SystemContext.setExecutorService(executorService);
            lockServer = new LockServer();
            LockServerCallable serverCallable = new LockServerCallable(lockServer);
            Future<Boolean> future = executorService.submit(serverCallable);
            LOGGER.debug("Lock Server submitted {}", future);
            return true;
        } else {
            LOGGER.debug("host is not primary or secondary lock server. quitting now");
            return false;
        }
    }

    @Override
    public void onStop() {
        if (!isStarted()) {
            LOGGER.debug("System is not started. No need to stop");
            return;
        }
        lockServer.onStop();
        lockServer = null;
        LOGGER.debug("Lock Server stopped");
        executorService.shutdown();
        executorService = null;
        LOGGER.debug("System shut down");
    }

    @Override
    public boolean isStarted() {
        return executorService != null;
    }
}
