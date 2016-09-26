package com.ohmyapp.lock.api;

import com.ohmyapp.lock.LockException;

import java.util.List;

/**
 * Created by Emerald on 9/15/2016.
 * server provider interface
 */
public interface ServerProvider {
    /**
     * init
     *
     * @return true if server is primary or secondary
     */
    boolean init() throws LockException;

    /**
     * create server list
     *
     * @return max server on a shard
     * @throws LockException le
     */
    int createServerList() throws LockException;

    /**
     * request a list of servers
     *
     * @return server list
     */
    List<String> requestServerList() throws LockException;

    /**
     * calculate shard
     *
     * @param hash hash string
     * @return shard #
     */
    int calculateShard(String hash);

    /**
     * return primary server from provider
     *
     * @param hash hash key
     * @return primary server
     */
    String returnServer(String hash);

    /**
     * return port for client to connect
     *
     * @param hash hash
     * @return port
     */
    int returnClientPort(String hash);

    /**
     * return server port
     *
     * @return server port
     */
    int returnServerPort();

    /**
     * return replica server port
     *
     * @return replica server port
     */
    int returnSecondaryPort();

    /**
     * return replica server port offset
     *
     * @return replica server port offset
     */
    int returnSecondaryPortOffset();

    /**
     * @return current leader index
     */
    int getCurrentLeader();

    /**
     * is primary server
     *
     * @return true or false
     */
    boolean isPrimary();

    /**
     * is secondary server
     *
     * @return true or false
     */
    boolean isSecondary();

    /**
     * @return host name
     */
    String getLocalhostName();
}
