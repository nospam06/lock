package com.ohmyapp.lock.impl.provider;

import com.ohmyapp.lock.LockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emerald on 9/15/2016.
 * provide list of servers
 */
public class ClasspathServerProvider extends AbstractServerProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathServerProvider.class);

    @Override
    public int createServerList() throws LockException {
        return readFromClasspath();
    }

    private int readFromClasspath() throws LockException {
        LOGGER.debug("reading server list from file");
        int max = 0;
        List<String> list = new ArrayList<>();
        serverList.add(list);
        String servers = "/servers.txt";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass()
                .getResourceAsStream(servers), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.debug(line);
                list.add(line);
                ++max;
            }
        } catch (IOException e) {
            throw new LockException(e.getMessage(), e);
        }
        return max;
    }
}
