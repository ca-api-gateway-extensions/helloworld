package com.l7tech.external.assertions.helloworld.server;

import com.l7tech.common.log.HybridDiagnosticContext;
import com.l7tech.common.mime.ContentTypeHeader;
import com.l7tech.common.mime.NoSuchPartException;
import com.l7tech.gateway.common.log.GatewayDiagnosticContextKeys;
import com.l7tech.gateway.common.transport.SsgConnector;
import com.l7tech.message.*;
import com.l7tech.objectmodel.EntityType;
import com.l7tech.objectmodel.Goid;
import com.l7tech.objectmodel.PersistentEntity;
import com.l7tech.policy.assertion.AssertionStatus;
import com.l7tech.server.MessageProcessor;
import com.l7tech.server.message.PolicyEnforcementContext;
import com.l7tech.server.policy.PolicyVersionException;
import com.l7tech.util.ExceptionUtils;
import com.l7tech.util.IOUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.l7tech.common.mime.ContentTypeHeader.XML_DEFAULT;
import static com.l7tech.common.mime.ContentTypeHeader.parseValue;
import static com.l7tech.gateway.common.transport.SsgConnector.PROP_OVERRIDE_CONTENT_TYPE;
import static com.l7tech.gateway.common.transport.SsgConnector.PROP_REQUEST_SIZE_LIMIT;
import static com.l7tech.message.Message.getMaxBytes;
import static com.l7tech.server.message.PolicyEnforcementContextFactory.createPolicyEnforcementContext;

/**
 * Transport Server Handler used to handle the incoming requests.
 * Every line of the input will be processed by the Gateway's message processor separately.
 * Processing ends if line matches to either 'quit' or 'exit'.
 */
public class HelloWorldTransportServerHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(HelloWorldTransportServerHandler.class.getName());

    private final SsgConnector connector;

    @Inject
    private MessageProcessor messageProcessor;

    public HelloWorldTransportServerHandler(final SsgConnector connector) {
        this.connector = connector;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        final String remoteAddress = httpExchange.getRemoteAddress().toString();
        final HttpRequestKnob requestKnob = HelloWorldTransportHelper.buildHttpRequestKnob(httpExchange);

        httpExchange.sendResponseHeaders(200, 0);
        String line;

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8.name()));
             final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(httpExchange.getResponseBody(), StandardCharsets.UTF_8.name()))) {
            while ((line = reader.readLine()) != null) {
                // call message processing with line as the message body
                if ("quit".equals(line) || "exit".equals(line)) {
                    break;
                }

                writer.write(processMessage(requestKnob, line, remoteAddress));
                writer.newLine();
                writer.flush();
            }
        }

        httpExchange.close();
    }

    /**
     * Prepares the incoming request line to the message processor.
     * @param requestKnob HttpRequestKnob for the message processor
     * @param message input line of text
     * @return prepared PolicyEnforcementContext instance
     * @throws IOException
     */
    private PolicyEnforcementContext preparePolicyContext(final HttpRequestKnob requestKnob, final String message) throws IOException {
        final PolicyEnforcementContext context = createPolicyEnforcementContext(null, null);
        final long requestSizeLimit = connector.getLongProperty(PROP_REQUEST_SIZE_LIMIT, getMaxBytes());
        final String ctypeStr = connector.getProperty(PROP_OVERRIDE_CONTENT_TYPE);
        final ContentTypeHeader ctype = ctypeStr == null ? XML_DEFAULT : parseValue(ctypeStr);

        context.getRequest().initialize(ctype, message.getBytes(StandardCharsets.UTF_8.name()), requestSizeLimit);
        context.getRequest().attachKnob(requestKnob, TlsKnob.class, TcpKnob.class);

        final Goid hardwiredServiceGoid = connector.getGoidProperty(EntityType.SERVICE, SsgConnector.PROP_HARDWIRED_SERVICE_ID, PersistentEntity.DEFAULT_GOID);
        if (!Goid.isDefault(hardwiredServiceGoid)) {
            context.getRequest().attachKnob(HasServiceId.class, new HasServiceIdImpl(hardwiredServiceGoid));
        }

        return context;
    }

    /**
     * Extracts the processed response from the message processor
     * @param context PolicyEnforcementContext instance
     * @return processed response from the message processor
     * @throws IOException
     */
    private String extractResponse(final PolicyEnforcementContext context) throws IOException {
        String response = "";

        try {
            response = new String(IOUtils.slurpStream(context.getResponse().getMimeKnob().getEntireMessageBodyAsInputStream()), StandardCharsets.UTF_8.name());
        } catch (NoSuchPartException e) {
            LOGGER.fine("Found response with no parts: " + ExceptionUtils.getMessage(e));
        }

        return response;
    }

    /**
     * Processes the message through message processor.
     * @param requestKnob HttpRequestKnob for the message processor
     * @param message input line of text
     * @param remoteAddress client address
     * @return processed response from the message processor.
     * Note: Returns error text if any error encounters.
     */
    private String processMessage(final HttpRequestKnob requestKnob, final String message, final String remoteAddress) {
        try {
            HybridDiagnosticContext.put(GatewayDiagnosticContextKeys.LISTEN_PORT_ID, connector.getGoid().toString());
            HybridDiagnosticContext.put(GatewayDiagnosticContextKeys.CLIENT_IP, remoteAddress);

            final PolicyEnforcementContext context = preparePolicyContext(requestKnob, message);
            final AssertionStatus status = messageProcessor.processMessage(context);
            if (AssertionStatus.NONE.equals(status)) {
                return extractResponse(context);
            } else {
                LOGGER.warning("Error encountered while processing the message: " + status);
                return "Error: " + status;
            }
        } catch (PolicyVersionException pve) {
            LOGGER.info("Request referred to an outdated version of policy");
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Error encountered while processing the message: " + ExceptionUtils.getMessage(t), ExceptionUtils.getDebugException(t));
        } finally {
            HybridDiagnosticContext.remove(GatewayDiagnosticContextKeys.LISTEN_PORT_ID);
            HybridDiagnosticContext.remove(GatewayDiagnosticContextKeys.CLIENT_IP);
        }

        return "Error: " + AssertionStatus.FAILED;
    }

}
