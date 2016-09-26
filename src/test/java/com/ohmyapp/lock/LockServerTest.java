package com.ohmyapp.lock;

import com.ohmyapp.lock.impl.client.LockClient;
import com.ohmyapp.lock.system.SystemBootStrap;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Emerald on 9/14/2016.
 * tests
 */
public class LockServerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockServerTest.class);

    @Test
    public void testLock() {
        try {
            // start bootStrap
            SystemBootStrap bootStrap = new SystemBootStrap();
            boolean serverStart = bootStrap.onStart();
            assertTrue("bootStrap did not start", serverStart);
// start client
            LockClient client = new LockClient();
            // lock
            boolean lock = client.lock("MyObject", "123");
            assertTrue("client lock was not successful", lock);
            // lock again
            boolean lock2 = client.lock("MyObject", "123");
            assertFalse("client lock was successful", lock2);
            // start client 2
            LockClient client2 = new LockClient();
            // lock
            boolean lock21 = client2.lock("MyObject", "123");
            assertFalse("client 2 lock was successful", lock21);
            // client 2 release
            boolean lock22 = client2.release("MyObject", "123");
            assertFalse("client 2 release was successful", lock22);
            // client 1 release
            boolean lock12 = client.release("MyObject", "123");
            assertTrue("client release was not successful", lock12);
            // lock
            boolean lock23 = client2.lock("MyObject", "123");
            assertTrue("client 2 lock was not successful", lock23);
            // lock again
            boolean lock13 = client.lock("MyObject", "123");
            assertFalse("client lock was successful", lock13);
            // stop client 2 to release lock
            client2.onStop();
            TimeUnit.SECONDS.sleep(1);
            // lock
            boolean lock14 = client.lock("MyObject", "123");
            assertTrue("client 2 lock was not successful", lock14);

            client.onStop();
            bootStrap.onStop();
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            fail();
        }
    }
}