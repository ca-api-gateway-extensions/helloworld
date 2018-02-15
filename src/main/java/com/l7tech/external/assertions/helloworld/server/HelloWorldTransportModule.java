package com.l7tech.external.assertions.helloworld.server;

import com.l7tech.gateway.common.Component;
import com.l7tech.gateway.common.LicenseManager;
import com.l7tech.gateway.common.transport.SsgConnector;
import com.l7tech.objectmodel.FindException;
import com.l7tech.objectmodel.Goid;
import com.l7tech.server.DefaultKey;
import com.l7tech.server.GatewayFeatureSets;
import com.l7tech.server.GatewayState;
import com.l7tech.server.LifecycleException;
import com.l7tech.server.audit.AuditContextUtils;
import com.l7tech.server.event.system.ReadyForMessages;
import com.l7tech.server.identity.cert.TrustedCertServices;
import com.l7tech.server.transport.ListenerException;
import com.l7tech.server.transport.SsgConnectorManager;
import com.l7tech.server.transport.TransportModule;
import com.l7tech.server.util.ApplicationEventProxy;
import com.l7tech.server.util.InjectingConstructor;
import com.l7tech.server.util.Injector;
import com.l7tech.util.Config;
import com.l7tech.util.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.l7tech.external.assertions.helloworld.HelloWorldConstants.HELLO_WORLD_SCHEME;
import static com.l7tech.external.assertions.helloworld.HelloWorldConstants.MODULE_IDENTITY_TAG;
import static com.l7tech.util.CollectionUtils.caseInsensitiveSet;
import static com.l7tech.util.ExceptionUtils.getDebugException;
import static com.l7tech.util.ExceptionUtils.getMessage;

public class HelloWorldTransportModule extends TransportModule implements ApplicationListener {

    private static final Logger LOGGER = Logger.getLogger(HelloWorldTransportModule.class.getName());
    private static final Set<String> SUPPORTED_SCHEMES = caseInsensitiveSet(HELLO_WORLD_SCHEME);

    private final Map<Goid, Pair<SsgConnector, HelloWorldTransportServer>> connectors = new ConcurrentHashMap<>();
    private final ApplicationEventProxy applicationEventProxy;
    private final GatewayState gatewayState;
    private final Injector injector;

    /**
     * Returns transport module instance.
     * @param context running application context
     * @return transport module instance
     */
    public static HelloWorldTransportModule getInstance(final ApplicationContext context) {
        final InjectingConstructor injector = context.getBean(InjectingConstructor.class);
        return injector.injectNew(HelloWorldTransportModule.class);
    }

    @Inject
    protected HelloWorldTransportModule(final ApplicationEventProxy applicationEventProxy,
                                        final LicenseManager licenseManager,
                                        final SsgConnectorManager ssgConnectorManager,
                                        final TrustedCertServices trustedCertServices,
                                        final DefaultKey defaultKey,
                                        final Config config,
                                        final GatewayState gatewayState,
                                        final Injector injector,
                                        final ApplicationContext applicationContext) {
        super(MODULE_IDENTITY_TAG + "Module",
                Component.GW_SERVER,
                LOGGER,
                GatewayFeatureSets.PROFILE_GATEWAY,
                licenseManager,
                ssgConnectorManager,
                trustedCertServices,
                defaultKey,
                config);
        this.applicationEventProxy = applicationEventProxy;
        this.gatewayState = gatewayState;
        this.injector = injector;
        setApplicationContext(applicationContext);
    }

    @Override
    protected Set<String> getSupportedSchemes() {
        return SUPPORTED_SCHEMES;
    }

    @Override
    protected boolean isCurrent(Goid goid, int version) {
        //checks to see if the connector is the given version
        final Pair<SsgConnector, HelloWorldTransportServer> entry = connectors.get(goid);
        return entry != null && entry.left.getVersion() == version;
    }

    @Override
    protected void addConnector(final SsgConnector connector) throws ListenerException {
        if (checkOnAddConnector(connector)) {
            // Remove any previous versions of this connector
            removeConnector(connector.getGoid());

            // Create transport server and keep track of it along with the connector
            final HelloWorldTransportServer transportServer = createAndStartTransportServer(connector);
            connectors.put(connector.getGoid(),
                    new Pair<>(connector.getReadOnlyCopy(), transportServer));
        }
    }

    @Override
    protected void removeConnector(Goid goid) {
        final Pair<SsgConnector, HelloWorldTransportServer> entry = connectors.remove(goid);
        if (entry != null) {
            auditStop(entry.left.getScheme(), describe(entry.left));
            entry.right.stop();
        }
    }

    @Override
    public void reportMisconfiguredConnector(Goid goid) {
        logger.log(Level.WARNING, MODULE_IDENTITY_TAG + " connector reported mis-configured: ID=" + goid);
    }

    @Override
    protected void doStart() throws LifecycleException {
        // Register the transport scheme(s) on module initialization
        registerSchemes();

        // Start the transport module's connectors if any
        if (gatewayState.isReadyForMessages()) {
            startConnectors();
        }
    }

    @Override
    protected void doStop() throws LifecycleException {
        // Stop the connectors owned by this transport module
        stopConnectors();

        // Unregister the transport scheme(s)
        unregisterSchemes();
    }

    @Override
    protected void doClose() throws LifecycleException {
        applicationEventProxy.removeApplicationListener(this);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        super.onApplicationEvent(applicationEvent);

        // Ensure starting the existing connectors on Gateway start
        if (applicationEvent instanceof ReadyForMessages) {
            startConnectors();
        }
    }

    /**
     * Performs series of checks on connector before creating transport server using it.
     * @param ssgConnector SsgConnector used to create transport server
     * @return true if transport server is allowed to create using specified connector.
     */
    private boolean checkOnAddConnector(final SsgConnector ssgConnector) {
        // Check if the connector is already activated with the same version
        if (isCurrent(ssgConnector.getGoid(), ssgConnector.getVersion())) {
            return false;
        }

        // validate th the connector is owned by this module
        if (!connectorIsOwnedByThisModule(ssgConnector)) {
            return false;
        }

        // validate that the gateway is properly licenced
        if (!isLicensed())
            return false;

        return true;
    }

    /**
     * Creates and starts transport server using connector.
     * @param connector SsgConnector used to create transport server
     * @return HelloWorld transport server instance
     * @throws ListenerException
     */
    private HelloWorldTransportServer createAndStartTransportServer(final SsgConnector connector) throws ListenerException {
        try {
            final HelloWorldTransportServer transportServer = newTransportServer(connector);
            transportServer.start();
            auditStart(connector.getScheme(), describe(connector));
            return transportServer;
        } catch (IllegalArgumentException iae) {
            throw new ListenerException(getMessage(iae), getDebugException(iae));
        } catch (Throwable t) {
            auditError(connector.getScheme(), "Error starting connector " + describe(connector), t);
            throw new ListenerException("Unable to create " + connector.getScheme() + " transport server: " + getMessage(t), t);
        }
    }

    /**
     * Returns new transport server instance.
     * @param connector SsgConnector instance used to create transprt server
     * @return HelloWorld transport server
     * @throws ListenerException
     * @throws IOException
     */
    private HelloWorldTransportServer newTransportServer(final SsgConnector connector) throws ListenerException, IOException {
        return new HelloWorldTransportServer(connector, this,
                HelloWorldTransportHelper.getBindAddress(ssgConnectorManager, connector), connector.getPort(), injector);
    }

    /**
     * Stops active connectors owned by this transport module.
     * This method is used to stop active connectors on module unload or on gateway shutdown.
     */
    private void stopConnectors() {
        final boolean wasSystem = AuditContextUtils.isSystem();
        final List<Goid> idsToStop = new ArrayList<>(connectors.keySet());

        AuditContextUtils.setSystem(true);

        for (final Goid goid : idsToStop) {
            try {
                removeConnector(goid);
            } catch (Exception e) {
                auditError(HELLO_WORLD_SCHEME, "Error while shutting down " + HELLO_WORLD_SCHEME + " connector with id: " + goid, e);
            }
        }

        AuditContextUtils.setSystem(wasSystem);
    }

    /**
     * Starts the existing connectors owned by this transport module.
     * This method is used to start existing connectors on module start-up or on gateway restart.
     */
    private void startConnectors() {
        final boolean wasSystem = AuditContextUtils.isSystem();

        try {
            AuditContextUtils.setSystem(true);
            final Collection<SsgConnector> connectors = ssgConnectorManager.findAll();
            for (final SsgConnector connector : connectors) {
                startConnector(connector);
            }
        } catch (FindException e) {
            auditError(HELLO_WORLD_SCHEME, "Unable to access existing connectors " + getMessage(e), e);
        } finally {
            AuditContextUtils.setSystem(wasSystem);
        }
    }

    /**
     * Stars the connector owned by this transport module.
     * @param connector SsgConnector owned by this transport module
     */
    private void startConnector(final SsgConnector connector) {
        if (connector.isEnabled() && connectorIsOwnedByThisModule(connector)) {
            try {
                addConnector(connector);
            } catch (Exception e) {
                auditError(HELLO_WORLD_SCHEME, "Unable to start " + connector.getScheme() + " connector on port " + connector.getPort() +
                        ": " + getMessage(e), getDebugException(e));
            }
        }
    }

    /**
     * Registers the module scheme(s).
     */
    private void registerSchemes() {
        ssgConnectorManager.registerTransportProtocol(
                HelloWorldTransportHelper.newTransportDescriptor(HELLO_WORLD_SCHEME), this);
    }

    /**
     * Unregisters the module scheme(s).
     */
    private void unregisterSchemes() {
        ssgConnectorManager.unregisterTransportProtocol(HELLO_WORLD_SCHEME);
    }

}
