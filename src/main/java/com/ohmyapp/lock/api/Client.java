package com.ohmyapp.lock.api;

/**
 * Created by Emerald on 9/15/2016.
 * lock client api
 */
public interface Client extends LifeCycle {
    /**
     * lock an object by key
     *
     * @param objectName object identifier, use for sharding
     * @param key        key
     * @return success or not
     */
    boolean lock(String objectName, String key);

    /**
     * release lock an object by key
     *
     * @param objectName object identifier, use for sharding
     * @param key        key
     * @return success or not
     */
    boolean release(String objectName, String key);
}
