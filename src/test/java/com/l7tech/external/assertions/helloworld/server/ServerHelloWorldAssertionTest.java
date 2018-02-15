/*
 * Copyright (c) 2017 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.l7tech.external.assertions.helloworld.server;

import com.l7tech.common.mime.NoSuchPartException;
import com.l7tech.external.assertions.helloworld.HelloWorldAssertion;
import com.l7tech.message.Message;
import com.l7tech.policy.assertion.AssertionStatus;
import com.l7tech.server.message.PolicyEnforcementContext;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

/**
 * Test the HelloWorldAssertion.
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerHelloWorldAssertionTest {

    @Mock
    private PolicyEnforcementContext policyEnforcementContext;

    private Message response;

    @Before
    public void before() {
        response = new Message();
        Mockito.when(policyEnforcementContext.getResponse()).thenReturn(response);
    }

    @Test
    public void testAssertion() throws IOException, NoSuchPartException {
        HelloWorldAssertion helloWorldAssertion = new HelloWorldAssertion();
        ServerHelloWorldAssertion serverHelloWorldAssertion = new ServerHelloWorldAssertion(helloWorldAssertion);

        AssertionStatus status = serverHelloWorldAssertion.checkRequest(policyEnforcementContext);

        Assert.assertEquals(AssertionStatus.NONE, status);
        Assert.assertEquals("Hello World!", IOUtils.toString(response.getMimeKnob().getEntireMessageBodyAsInputStream()));
    }

}
