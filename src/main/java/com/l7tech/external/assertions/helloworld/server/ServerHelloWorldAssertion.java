/*
 * Copyright (c) 2017 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.l7tech.external.assertions.helloworld.server;

import com.l7tech.common.mime.ContentTypeHeader;
import com.l7tech.external.assertions.helloworld.HelloWorldAssertion;
import com.l7tech.policy.assertion.AssertionStatus;
import com.l7tech.policy.assertion.PolicyAssertionException;
import com.l7tech.server.message.PolicyEnforcementContext;
import com.l7tech.server.policy.assertion.AbstractServerAssertion;

import java.io.IOException;

/**
 * Server side implementation of the HelloWorldAssertion.
 *
 * @see com.l7tech.external.assertions.helloworld.HelloWorldAssertion
 */
@SuppressWarnings("unused")
public class ServerHelloWorldAssertion extends AbstractServerAssertion<HelloWorldAssertion> {
    private final String[] variablesUsed;

    public ServerHelloWorldAssertion(final HelloWorldAssertion assertion) {
        super(assertion);

        this.variablesUsed = assertion.getVariablesUsed();
    }

    public AssertionStatus checkRequest(final PolicyEnforcementContext context) throws IOException {
        context.getResponse().initialize(ContentTypeHeader.TEXT_DEFAULT, "Hello World!".getBytes());
        return AssertionStatus.NONE;
    }

    /*
     * Called reflectively by module class loader when module is unloaded, to ask us to clean up any globals
     * that would otherwise keep our instances from getting collected.
     *
     * DELETEME if not required.
     */
    public static void onModuleUnloaded() {
        // This assertion doesn't have anything to do in response to this, but it implements this anyway
        // since it will be used as an example by future modular assertion authors
    }
}
