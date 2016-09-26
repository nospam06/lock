package com.ohmyapp.lock;

/**
 * Created by Emerald on 9/15/2016.
 * exception
 */
public class LockException extends Exception {
    public LockException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
