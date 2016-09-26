package com.ohmyapp.lock.main;

/**
 * Created by Emerald on 9/14/2016.
 * tests
 */
public class LockShardSystemTest extends LockSystemTest {

    public static void main(String[] args) {
        LockShardSystemTest main = new LockShardSystemTest();
        main.testSystem("com.ohmyapp.lock.stub.ShardServerProvider");
    }
}