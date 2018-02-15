package com.l7tech.external.assertions.helloworld.server;

import com.l7tech.gateway.common.transport.SsgConnector;
import com.l7tech.server.util.Injector;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * HTTP based transport server used to process inbound requests from the designated listen port.
 */
public class HelloWorldTransportServer {

    private final SsgConnector connector;
    private final Injector injector;
    private final HttpServer httpServer;
    private HttpContext httpContext;

    public HelloWorldTransportServer(final SsgConnector connector, final InetAddress listenAddress, final int listenPort, final Injector injector) throws IOException {
        this.connector = connector;
        this.injector = injector;

        this.httpServer = HttpServer.create(new InetSocketAddress(listenAddress, listenPort), 0);
    }

    public void stop() {
        if (httpContext != null) {
            httpServer.removeContext(httpContext);
        }

        httpServer.stop(5);
    }

    public void start() {
        httpContext = httpServer.createContext("/", newTransportServerHandler(connector));
        httpServer.start();
    }

    private HelloWorldTransportServerHandler newTransportServerHandler(final SsgConnector connector) {
        HelloWorldTransportServerHandler transportServerHandler = new HelloWorldTransportServerHandler(connector);
        injector.inject(transportServerHandler);
        return transportServerHandler;
    }
}
