package com.ohmyapp.lock.api;

/**
 * Created by Emerald on 9/18/2016.
 * publish changes
 */
public interface Publisher {
    /**
     * public change messages
     *
     * @param message changes
     */
    void publishMessage(String message);
}
