package com.ohmyapp.lock.system;

import com.ohmyapp.lock.LockException;
import com.ohmyapp.lock.api.ServerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Emerald on 9/18/2016.
 * main class
 */
public class SystemMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemMain.class);

    public static void main(String[] args) {
        SystemMain main = new SystemMain();
        main.run(args);
    }

    /**
     * @param args command line args
     */
    public void run(String[] args) {
        try {
            if (args.length > 0) {
                Object provider = Class.forName(args[0]).newInstance();
                SystemContext.setProvider((ServerProvider) provider);
            }
            SystemBootStrap bootStrap = new SystemBootStrap();
            bootStrap.onStart();
            LOGGER.debug("finished bootup");
        } catch (LockException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
