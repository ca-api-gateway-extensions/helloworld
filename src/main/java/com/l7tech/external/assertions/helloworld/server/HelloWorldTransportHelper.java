package com.l7tech.external.assertions.helloworld.server;

import com.l7tech.common.http.HttpCookie;
import com.l7tech.common.http.HttpMethod;
import com.l7tech.external.assertions.helloworld.HelloWorldAssertion;
import com.l7tech.gateway.common.transport.SsgConnector;
import com.l7tech.gateway.common.transport.TransportDescriptor;
import com.l7tech.message.HttpRequestKnob;
import com.l7tech.server.transport.ListenerException;
import com.l7tech.server.transport.SsgConnectorManager;
import com.l7tech.util.InetAddressUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Map;

public final class HelloWorldTransportHelper {

    public static TransportDescriptor newTransportDescriptor(final String scheme) {
        final TransportDescriptor descriptor = new TransportDescriptor();

        descriptor.setSupportsSpecifiedContentType(false);
        descriptor.setRequiresSpecifiedContentType(false);

        //this forces choosing a service to execute
        descriptor.setSupportsHardwiredServiceResolution(true);
        descriptor.setRequiresHardwiredServiceResolutionForNonXml(true);
        descriptor.setRequiresHardwiredServiceResolutionAlways(true);

        descriptor.setUsesTls(false);
        descriptor.setSupportsPrivateThreadPool(false);
        descriptor.setModularAssertionClassname(HelloWorldAssertion.class.getName());
        descriptor.setScheme(scheme);

        return descriptor;
    }

    public static InetAddress getBindAddress(final SsgConnectorManager ssgConnectorManager, final SsgConnector connector) throws ListenerException, UnknownHostException {
        String bindAddress = connector.getProperty(SsgConnector.PROP_BIND_ADDRESS);

        if (bindAddress == null || InetAddressUtil.isAnyHostAddress(bindAddress)) {
            bindAddress = InetAddressUtil.getAnyHostAddress();
        } else {
            bindAddress = ssgConnectorManager.translateBindAddress(bindAddress, connector.getPort());
        }

        return InetAddress.getByName(bindAddress);
    }

    public static HttpRequestKnob buildHttpRequestKnob(final HttpExchange httpExchange) {
        return new HttpRequestKnob() {
            @Override
            public String getRequestUri() {
                return httpExchange.getRequestURI().toString();
            }

            @Override
            public X509Certificate[] getClientCertificate() throws IOException {
                return new X509Certificate[0];
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public String getRemoteAddress() {
                return httpExchange.getRemoteAddress().toString();
            }

            @Override
            public String getRemoteHost() {
                return httpExchange.getRemoteAddress().getHostString();
            }

            @Override
            public int getRemotePort() {
                return httpExchange.getRemoteAddress().getPort();
            }

            @Override
            public String getLocalAddress() {
                return httpExchange.getLocalAddress().toString();
            }

            @Override
            public String getLocalHost() {
                return httpExchange.getLocalAddress().getHostString();
            }

            @Override
            public int getLocalPort() {
                return httpExchange.getLocalAddress().getPort();
            }

            @Override
            public int getLocalListenerPort() {
                return httpExchange.getLocalAddress().getPort();
            }

            @Override
            public String getSoapAction() throws IOException {
                return null;
            }

            @Override
            public String[] getHeaderValues(String s) {
                return httpExchange.getRequestHeaders().values().stream().map(Object::toString).toArray(String[]::new);
            }

            @Override
            public String[] getHeaderNames() {
                return httpExchange.getRequestHeaders().keySet().toArray(new String[httpExchange.getRequestHeaders().keySet().size()]);
            }

            @Override
            public HttpCookie[] getCookies() {
                return new HttpCookie[0];
            }

            @Override
            public HttpMethod getMethod() {
                return HttpMethod.valueOf(httpExchange.getRequestMethod());
            }

            @Override
            public String getMethodAsString() {
                return httpExchange.getRequestMethod();
            }

            @Override
            public String getRequestUrl() {
                return httpExchange.getRequestURI().toString();
            }

            @Override
            public URL getRequestURL() {
                try {
                    return httpExchange.getRequestURI().toURL();
                } catch (MalformedURLException e) {
                    return null;
                }
            }

            @Override
            public long getDateHeader(String s) throws ParseException {
                return Long.valueOf(httpExchange.getRequestHeaders().get(s).toString());
            }

            @Override
            public int getIntHeader(String s) {
                return Integer.valueOf(httpExchange.getRequestHeaders().get(s).toString());
            }

            @Override
            public String getHeaderFirstValue(String s) {
                return httpExchange.getRequestHeaders().getFirst(s);
            }

            @Override
            public String getHeaderSingleValue(String s) throws IOException {
                return httpExchange.getRequestHeaders().getFirst(s);
            }

            @Override
            public String getParameter(String s) throws IOException {
                return null;
            }

            @Override
            public Map getParameterMap() throws IOException {
                return null;
            }

            @Override
            public String[] getParameterValues(String s) throws IOException {
                return new String[0];
            }

            @Override
            public Enumeration getParameterNames() throws IOException {
                return null;
            }

            @Override
            public Object getConnectionIdentifier() {
                return null;
            }

            @Override
            public String getQueryString() {
                return null;
            }
        };
    }

}
