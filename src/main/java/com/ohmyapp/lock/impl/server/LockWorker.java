package com.ohmyapp.lock.impl.server;

import com.ohmyapp.lock.LockException;
import com.ohmyapp.lock.api.Server;
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
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Emerald on 9/13/2016.
 * Socket Worker
 */
class LockWorker implements Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockWorker.class);
    private static final ServerProvider PROVIDER = SystemContext.getProvider();
    private final String id = UUID.randomUUID().toString();

    private ConcurrentMap<String, ConcurrentMap<String, String>> lockMap;
    private ConcurrentMap<String, ConcurrentMap<String, String>> myLockMap = new ConcurrentHashMap<>();
    private Socket socket;

    LockWorker(Socket inSocket, ConcurrentMap<String, ConcurrentMap<String, String>> inLockMap) {
        socket = inSocket;
        lockMap = inLockMap;
    }

    @Override
    public boolean onStart() throws LockException {
        return true;
    }

    @Override
    public void onRun() {
        try (BufferedReader inReader = LockUtils.createReader(socket);
             PrintWriter outWriter = LockUtils.createWriter(socket)) {
            while (true) {
                LOGGER.debug("{} - ready to accept lock request on port {}", id, socket.getLocalPort());
                String request = inReader.readLine();
                if (request == null) {
                    // if request is null, client socket closed
                    LOGGER.debug("{} - session disconnected. release local locks", id);
                    onStop();
                    break;
                }
                LOGGER.debug(request);
                LockRequest lockRequest = SystemContext.GSON.fromJson(request, LockRequest.class);
                LockResponse lockResponse = new LockResponse();
                lockResponse.setResponderId(getId());
                String action = lockRequest.getAction();
                if (LockRequest.LOCK.equals(action)) {
                    // lock core logic
                    onLock(lockRequest, lockResponse);
                }
                if (LockRequest.RELEASE.equals(action)) {
                    // lock core logic
                    onRelease(lockRequest, lockResponse);
                }
                if (LockRequest.SYNC.equals(action) && PROVIDER.isPrimary()) {
                    // sync replica
                    onSync(lockResponse);
                }
                if (LockRequest.PING.equals(action)) {
                    // sync replica
                    onLeader(lockResponse);
                }
                String response = SystemContext.GSON.toJson(lockResponse);
                outWriter.println(response);
                outWriter.flush();
                if (lockResponse.isSuccess() && PROVIDER.isPrimary()
                        && (LockRequest.LOCK.equals(action) || LockRequest.RELEASE.equals(action))) {
                    SystemContext.getPublisher().publishMessage(request);
                }
            }
        } catch (IOException e) {
            LOGGER.error(id + " - caught " + e.getMessage(), e);
        }
    }

    private void onLock(LockRequest lockRequest, LockResponse lockResponse) {
        String objectName = lockRequest.getObjectName();
        ConcurrentMap<String, String> objectMap = lockMap.get(objectName);
        if (objectMap == null) {
            objectMap = new ConcurrentHashMap<>();
            lockMap.put(objectName, objectMap);
        }
        String existingLockId = objectMap.putIfAbsent(lockRequest.getKey(), lockRequest.getRequesterId());
        if (existingLockId == null) {
            lockResponse.setSuccess(true);
            LOGGER.debug("{} - {} successful {} {} by {}", id, lockRequest.getAction(),
                    lockRequest.getObjectName(), lockRequest.getKey(), lockRequest.getRequesterId());
            // keep local copy for cleaning up
            keepLocalCopy(lockRequest);
        } else {
            LOGGER.error("existing lock exists {}", existingLockId);
        }
    }

    private void onRelease(LockRequest lockRequest, LockResponse lockResponse) {
        String objectName = lockRequest.getObjectName();
        ConcurrentMap<String, String> objectMap = lockMap.get(objectName);
        if (objectMap == null) {
            return;
        }
        String key = lockRequest.getKey();
        String requesterId = objectMap.get(key);
        if (requesterId.equals(lockRequest.getRequesterId())) {
            // only same client can release lock
            objectMap.remove(key);
            if (objectMap.isEmpty()) {
                lockMap.remove(objectName);
            }
            lockResponse.setSuccess(true);
            LOGGER.debug("{} - {} successful {} {} by {}", id, lockRequest.getAction(),
                    lockRequest.getObjectName(), lockRequest.getKey(), lockRequest.getRequesterId());
            removeLocalCopy(lockRequest);
        }
    }

    private void onSync(LockResponse lockResponse) {
        String respond = SystemContext.GSON.toJson(lockMap);
        LOGGER.debug("sending sync to replica {}", respond);
        lockResponse.setSuccess(true);
        lockResponse.setRespond(respond);
    }

    private void onLeader(LockResponse lockResponse) {
        lockResponse.setSuccess(true);
        lockResponse.setRespond(SystemContext.getProvider().getLocalhostName());
        lockResponse.setResponderId(id);
    }

    private void keepLocalCopy(LockRequest lockRequest) {
        // keep local copy for cleaning up
        String objectName = lockRequest.getObjectName();
        ConcurrentMap<String, String> myObjectMap = myLockMap.get(objectName);
        if (myObjectMap == null) {
            myObjectMap = new ConcurrentHashMap<>();
            myLockMap.put(objectName, myObjectMap);
        }
        myObjectMap.putIfAbsent(lockRequest.getKey(), lockRequest.getRequesterId());
    }

    private void removeLocalCopy(LockRequest lockRequest) {
        // remove local copy, cleaning up
        String objectName = lockRequest.getObjectName();
        ConcurrentMap<String, String> myObjectMap = myLockMap.get(objectName);
        if (myObjectMap == null) {
            return;
        }
        myObjectMap.remove(lockRequest.getKey());
        if (myObjectMap.isEmpty()) {
            myLockMap.remove(objectName);
        }
    }

    @Override
    public void onStop() {
        try {
            releaseMyLock();
            if (socket != null && socket.isConnected()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    private void releaseMyLock() {
        for (Map.Entry<String, ConcurrentMap<String, String>> entry : myLockMap.entrySet()) {
            String objectName = entry.getKey();
            ConcurrentMap<String, String> objectMap = lockMap.get(objectName);
            for (String key : entry.getValue().keySet()) {
                String requesterId = objectMap.remove(key);
                LockRequest lockRequest = new LockRequest();
                lockRequest.setAction(LockRequest.RELEASE);
                lockRequest.setObjectName(objectName);
                lockRequest.setKey(key);
                lockRequest.setRequesterId(requesterId);
                String request = SystemContext.GSON.toJson(lockRequest);
                SystemContext.getPublisher().publishMessage(request);
            }
            if (objectMap.isEmpty()) {
                lockMap.remove(objectName);
            }
        }
        myLockMap.clear();
    }

    String getId() {
        return id;
    }
}
