package com.ohmyapp.lock.stub;

import com.ohmyapp.lock.LockException;
import com.ohmyapp.lock.impl.provider.AbstractServerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emerald on 9/18/2016.
 * test server provider
 */
public class ShardServerProvider extends AbstractServerProvider {
    public static final Logger LOGGER = LoggerFactory.getLogger(ShardServerProvider.class);

    @Override
    public int createServerList() throws LockException {
        serverList.clear();
        List<String> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        serverList.add(list);
        serverList.add(list2);
        list.add("server1");
        list.add("server2");
        list.add("server3");
        list2.add("server4");
        list2.add("server5");
        list2.add("server6");
        return 3;
    }

    @Override
    protected String findLocalHostName() {
        String hostName = System.getProperty("com.ohmyapp.lock.server");
        LOGGER.info("hostname = {}", hostName);
        return hostName;
    }
}
