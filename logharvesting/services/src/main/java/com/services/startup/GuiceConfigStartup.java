package com.services.startup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris_ge
 */
public class GuiceConfigStartup {

    private static final Logger logger = LoggerFactory
            .getLogger(GuiceConfigStartup.class.getName());

    public GuiceConfigStartup() {
        // setSystemProperties();
        setupInjector();

    }

    public static void main(String[] args) {
        new GuiceConfigStartup();
    }

    protected Injector getInjector() {
        return setupInjector();
    }

    private Injector setupInjector() {
        Injector injector = null;
        try {
            injector = Guice.createInjector(new StartupServicesModule());
            logger.warn("GuiceInit[Success]");

        } catch (Throwable t) {
            failServiceStartup(t, "GuiceInit[Fail]");
        }

        try {

            //Key.get(Service.class, DatabaseService.class));

            ServiceStartup startup = injector.getInstance(ServiceStartup.class);
            logger.warn("GuiceInit[Success] Services Startup[Get class]");
            logger.warn("GuiceInit[Sucess] Services Startup[Attempt]");
            try {
                startup.start();
                logger.warn("GuiceInit[Success] Startup[Success]");
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } catch (Throwable t) {
            failServiceStartup(t, "GuiceInit[Success] Services Startup[fail]");
        }
        return injector;
    }

    /*
        private void setSystemProperties() {
            // Any system properties that should be set for all servers on all tiers
            // can be set here.
            // This lets us bypass having to add a bunch of "-D..." JVM options
            // everywhere.
            if (System.getProperty("jef.system.home") == null) {
                System.setProperty("jef.system.home",
                        workingPath.getDirectory("properties/").getPath());
                // System.getProperty("user.name")
            }
            if (System.getProperty("jef.system.environmentFileName") == null) {
                System.setProperty("jef.system.environmentFileName",
                        "jef-aws.environment.qa.xml");

            }
        }
    */
    private void failServiceStartup(Throwable t, String message) {
        int counter = 0;
        logger.warn(message + " #" + counter + " ", t);
        if (t.getCause() != null) {
            for (Throwable cause = t.getCause(); cause != null; ) {
                counter++;
                logger.warn(message + " #" + counter + " ", cause);
                cause = cause.getCause();
            }

        }
        logger.warn(".......................Failing com.services.startup");
        throw new RuntimeException(t);
    }
}
