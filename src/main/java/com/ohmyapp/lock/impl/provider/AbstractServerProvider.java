package com.ohmyapp.lock.impl.provider;

import com.ohmyapp.lock.LockException;
import com.ohmyapp.lock.api.ServerProvider;
import com.ohmyapp.lock.pojo.LockRequest;
import com.ohmyapp.lock.pojo.LockResponse;
import com.ohmyapp.lock.system.SystemContext;
import com.ohmyapp.lock.utils.LockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Emerald on 9/15/2016.
 * provide list of servers
 */
public abstract class AbstractServerProvider implements ServerProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServerProvider.class);
    private final String localhostName = findLocalHostName();
    protected List<List<String>> serverList = new ArrayList<>();
    private int shard;
    private int shardSize;
    private int position;
    private boolean primary;
    private boolean secondary;
    private int currentLeader = 0;

    @Override
    public boolean init() throws LockException {
        primary = false;
        secondary = false;
        shardSize = createServerList();
        boolean server = isServer();
        if (server) {
            determineLeader();
        }
        return server;
    }

    private boolean isServer() {
        shard = 0;
        position = 0;
        for (List<String> shards : serverList) {
            position = shards.indexOf(localhostName);
            if (position >= 0) {
                LOGGER.debug("{} is a server in shard {}, position {}", localhostName, shard, position);
                return true;
            }
            ++shard;
        }
        return false;
    }

    private void determineLeader() {
        List<String> servers = serverList.get(shard);
        // connect to server port to see leader is up
        currentLeader = 0;
        for (String server : servers) {
            String leader = connectToLeader(server);
            if (!leader.isEmpty()) {
                secondary = true;
                currentLeader = servers.indexOf(leader);
                return;
            }
        }
        // if no leader currently, the first host assumes primary
        currentLeader = 0;
        if (position == 0) {
            primary = true;
            return;
        }
        // wait 5 seconds for each position to avoid collision
        try {
            TimeUnit.SECONDS.sleep(position * 3);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        for (int i = 0; i < position; ++i) {
            String server = servers.get(i);
            String leader = connectToLeader(server);
            if (!leader.isEmpty()) {
                secondary = true;
                LOGGER.debug("{} found as leader", leader);
                currentLeader = servers.indexOf(leader);
                return;
            }
        }
        // no leader found in front of the list
        primary = true;
        currentLeader = position;
    }

    private String connectToLeader(String server) {
        if (server.equals(localhostName)) {
            return "";
        }
        LOGGER.debug("attempt to connect to {} at port {}", server, returnServerPort());
        try (Socket socket = new Socket(server, returnServerPort())) {
            PrintWriter writer = LockUtils.createWriter(socket);
            LockRequest request = new LockRequest();
            request.setAction(LockRequest.PING);
            request.setRequesterId(localhostName);
            String requestString = SystemContext.GSON.toJson(request);
            writer.println(requestString);
            writer.flush();
            BufferedReader reader = LockUtils.createReader(socket);
            String respond = reader.readLine();
            LockResponse response = SystemContext.GSON.fromJson(respond, LockResponse.class);
            return response.getRespond();
        } catch (IOException e) {
            LOGGER.debug("{} is not primary", server);
        }
        return "";
    }

    @Override
    public List<String> requestServerList() throws LockException {
        if (serverList.isEmpty()) {
            init();
            if (serverList.isEmpty()) {
                LOGGER.warn("no server is found on server list");
                return Collections.unmodifiableList(Collections.emptyList());
            }
        }
        return Collections.unmodifiableList(serverList.get(shard));
    }

    @Override
    public String returnServer(String hash) {
        int shard = calculateShard(hash);
        return serverList.get(shard).get(currentLeader);
    }

    public int calculateShard(String hash) {
        // simple hash algorithm used
        int hashCode = hash.hashCode();
        hashCode = hashCode > 0 ? hashCode : 0 - hashCode;
        return hashCode % serverList.size();
    }

    @Override
    public boolean isPrimary() {
        return primary;
    }

    @Override
    public boolean isSecondary() {
        return secondary;
    }

    @Override
    public int returnClientPort(String hash) {
        return SystemContext.getServerPort() + calculateShard(hash);
    }

    @Override
    public int returnServerPort() {
        return SystemContext.getServerPort() + shard;
    }

    @Override
    public int returnSecondaryPort() {
        return returnSecondaryPortOffset() + position;
    }

    @Override
    public int returnSecondaryPortOffset() {
        return SystemContext.getServerPort() + shardSize * (serverList.size() + shard);
    }

    @Override
    public int getCurrentLeader() {
        return currentLeader;
    }

    @Override
    public String getLocalhostName() {
        return localhostName;
    }

    protected String findLocalHostName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            LOGGER.error("Cannot read localhost name", e);
            return "localhost";
        }
    }
}
