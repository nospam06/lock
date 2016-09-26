package com.ohmyapp.lock.impl.messaging;

import com.ohmyapp.lock.api.Publisher;
import com.ohmyapp.lock.system.SystemContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Emerald on 9/14/2016.
 * publish change to followers
 */
public class LockPublisher implements Publisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockPublisher.class);

    @Override
    public void publishMessage(String message) {
        BlockingQueue<String> queue = SystemContext.getQueue();
        boolean offer = queue.offer(message);
        if (offer) {
            LOGGER.debug("change published {}", message);
        } else {
            LOGGER.error("change failed to publish {}", message);
        }
    }
}
