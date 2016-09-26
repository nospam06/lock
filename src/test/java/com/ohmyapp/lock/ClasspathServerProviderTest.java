package com.ohmyapp.lock;

import com.ohmyapp.lock.api.ServerProvider;
import com.ohmyapp.lock.impl.provider.ClasspathServerProvider;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Emerald on 9/15/2016.
 * test
 */
public class ClasspathServerProviderTest {
    @Test
    public void requestServerList() throws Exception {
        ServerProvider provider = new ClasspathServerProvider();
        List<String> serverList = provider.requestServerList();
        assertNotNull(serverList);
        assertFalse(serverList.isEmpty());
    }
}