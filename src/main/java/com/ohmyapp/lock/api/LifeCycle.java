package com.ohmyapp.lock.api;

import com.ohmyapp.lock.LockException;

/**
 * Created by Emerald on 9/16/2016.
 * life cycle
 */
public interface LifeCycle {

    /**
     * start
     *
     * @return true if started
     * @throws LockException ex
     */
    boolean onStart() throws LockException;

    /**
     * stop
     */
    void onStop();

    /**
     * check system is started
     *
     * @return true or false
     */
    boolean isStarted();
}
