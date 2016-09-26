package com.ohmyapp.lock.api;

/**
 * Created by Emerald on 9/15/2016.
 * lock client api
 */
public interface Server extends LifeCycle {

    /**
     * start lock server
     */
    void onRun();
}
