package com.ohmyapp.lock.pojo;

import java.io.Serializable;

/**
 * Created by Emerald on 9/16/2016.
 * lock response pojo
 */
public class LockResponse implements Serializable {
    private String responderId;
    private String respond;
    private boolean success = false;

    /**
     * responder id
     *
     * @return responder id
     */
    public String getResponderId() {
        return responderId;
    }

    /**
     * @param responderId responder id
     */
    public void setResponderId(String responderId) {
        this.responderId = responderId;
    }

    /**
     * @return respond
     */
    public String getRespond() {
        return respond;
    }

    /**
     * @param respond respond
     */
    public void setRespond(String respond) {
        this.respond = respond;
    }

    /**
     * success flag
     *
     * @return true if lock request is successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * set success
     *
     * @param inSuccess success
     */
    public void setSuccess(boolean inSuccess) {
        success = inSuccess;
    }
}
