package com.ohmyapp.lock.pojo;

import java.io.Serializable;

/**
 * Created by Emerald on 9/16/2016.
 * pojo for lock request
 */
public class LockRequest implements Serializable {
    public static final String LOCK = "Lock";
    public static final String RELEASE = "Release";
    public static final String SYNC = "Sync";
    public static final String PING = "Ping";

    private String action;
    private String requesterId;
    private String objectName;
    private String key;

    /**
     * lock action
     *
     * @return lock
     */
    public String getAction() {
        return action;
    }

    /**
     * lock action
     *
     * @param action action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * requesterId
     *
     * @return requesterId
     */
    public String getRequesterId() {
        return requesterId;
    }

    /**
     * requesterId.
     *
     * @param requesterId requesterId
     */
    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    /**
     * lock object identifier
     *
     * @return lock object identifier
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * set object identifier
     *
     * @param objectName object identifier
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * lock key
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * set key
     *
     * @param key key
     */
    public void setKey(String key) {
        this.key = key;
    }
}
