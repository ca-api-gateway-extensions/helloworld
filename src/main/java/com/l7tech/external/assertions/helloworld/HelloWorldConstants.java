package com.l7tech.external.assertions.helloworld;

public final class HelloWorldConstants {

    public static final String MODULE_IDENTITY_TAG = "HelloWorld";
    public static final String HELLO_WORLD_SCHEME = MODULE_IDENTITY_TAG + ".SCHEME";
    public static final String MODULE_IDENTITY_FRIENDLY_TAG = "Hello World";

    private HelloWorldConstants() {
        throw new IllegalStateException("Cannot instantiate a utility class.");
    }
}
