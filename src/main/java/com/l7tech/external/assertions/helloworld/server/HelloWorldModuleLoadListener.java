package com.l7tech.external.assertions.helloworld.server;

import com.l7tech.external.assertions.helloworld.HelloWorldConstants;
import com.l7tech.server.LifecycleException;
import com.l7tech.util.ExceptionUtils;
import org.springframework.context.ApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class HelloWorldModuleLoadListener {

    private static final Logger LOGGER = Logger.getLogger(HelloWorldModuleLoadListener.class.getName());
    private static HelloWorldTransportModule transportModule;

    /*
     * Called reflectively by module class loader when module is loaded, to provision to perform module level initialization.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static synchronized void onModuleLoaded(ApplicationContext context) {
        if (transportModule != null) {
            LOGGER.log(Level.WARNING, HelloWorldConstants.MODULE_IDENTITY_TAG + " transport module is already initialized");
        } else {
            LOGGER.log(Level.INFO, HelloWorldConstants.MODULE_IDENTITY_TAG + " transport module is starting");
            transportModule = HelloWorldTransportModule.getInstance(context);

            try {
                transportModule.start();
            } catch (LifecycleException e) {
                LOGGER.log(Level.WARNING,
                        HelloWorldConstants.MODULE_IDENTITY_TAG + " transport module threw exception on startup: " + ExceptionUtils.getMessage(e), e);
            }
        }
    }

    /*
     * Called reflectively by module class loader when module is unloaded, to ask us to clean up any globals
     * that would otherwise keep our instances from getting collected.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static synchronized void onModuleUnloaded() {
        if (transportModule != null) {
            LOGGER.log(Level.INFO, HelloWorldConstants.MODULE_IDENTITY_TAG + " transport module is shutting down");

            try {
                transportModule.destroy();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        HelloWorldConstants.MODULE_IDENTITY_TAG + " transport module threw exception on shutdown: " + ExceptionUtils.getMessage(e), e);
            } finally {
                transportModule = null;
            }
        }
    }
}
