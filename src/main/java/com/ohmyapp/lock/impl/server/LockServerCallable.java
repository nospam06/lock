package com.ohmyapp.lock.impl.server;

import com.ohmyapp.lock.api.Server;

import java.util.concurrent.Callable;

/**
 * Created by Emerald on 9/13/2016.
 * Socket Worker
 */
public class LockServerCallable implements Callable<Boolean> {
    private Server server;

    public LockServerCallable(Server inServer) {
        server = inServer;
    }

    @Override
    public Boolean call() throws Exception {
        boolean started = server.onStart();
        if (started) {
            server.onRun();
        } else {
            server.onStop();
        }
        return true;
    }
}
