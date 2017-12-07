/*
 * Copyright (c) 2017 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.l7tech.external.assertions.helloworld.console;

import com.l7tech.console.panels.AssertionPropertiesOkCancelSupport;
import com.l7tech.external.assertions.helloworld.HelloWorldAssertion;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("unused")
public class HelloWorldAssertionPropertiesDialog extends AssertionPropertiesOkCancelSupport<HelloWorldAssertion> {
    private JPanel mainPanel = new JPanel();

    public HelloWorldAssertionPropertiesDialog(final Window parent,
                                               final HelloWorldAssertion assertion) {
        super(HelloWorldAssertion.class, parent, assertion, true);
        initComponents();
        setData(assertion);
    }

    @Override
    public void setData(HelloWorldAssertion helloWorldAssertion) {
        //sets data on the given helloWorld Assertion
    }

    @Override
    public HelloWorldAssertion getData(HelloWorldAssertion helloWorldAssertion) {
        return helloWorldAssertion;
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        mainPanel.add(new JLabel("Hello World"));

    }

    @Override
    protected JPanel createPropertyPanel() {
        return mainPanel;
    }
}
