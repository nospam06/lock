package com.ohmyapp.lock.main;

import com.ohmyapp.lock.impl.client.LockClient;
import com.ohmyapp.lock.system.SystemMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Emerald on 9/14/2016.
 * tests
 */
public class LockSystemTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockSystemTest.class);

    public static void main(String[] args) {
        LockSystemTest main = new LockSystemTest();
        main.testSystem("com.ohmyapp.lock.stub.StubServerProvider");
    }

    void testSystem(String... args) {
        try {
            // start bootStrap
            SystemMain main = new SystemMain();
            main.run(args);
// start client
            LOGGER.debug("start client");
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

            // lock
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    for (int i = 1000; i < 5000; ++i) {
                        for (int j = 0; j < 10; ++j) {
                            client.lock("Lock" + i, Integer.toString(j));
                        }
                    }
                }
            };
            TimerTask task2 = new TimerTask() {
                @Override
                public void run() {
                    for (int i = 1000; i < 5000; ++i) {
                        for (int j = 0; j < 10; ++j) {
                            client2.lock("Lock" + i, Integer.toString(j));
                        }
                    }
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 0L);
            Timer timer2 = new Timer();
            timer2.schedule(task2, 0L);
            client.onStop();
            client2.onStop();
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            fail();
        }
    }
}