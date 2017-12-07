/*
 * Copyright (c) 2017 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.l7tech.external.assertions.helloworld;

import org.junit.Test;

/**
 * Test the HelloWorldAssertion.
 */
public class HelloWorldAssertionTest {

    @Test
    public void testAssertion() {
        HelloWorldAssertion helloWorldAssertion = new HelloWorldAssertion();
        helloWorldAssertion.meta();
    }
}
