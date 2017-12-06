package com.l7tech.external.assertions.helloworld.console;

import com.l7tech.console.panels.AssertionPropertiesOkCancelSupport;
import com.l7tech.external.assertions.helloworld.HelloWorldAssertion;

import javax.swing.*;
import java.awt.*;

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

    }

    @Override
    public HelloWorldAssertion getData(HelloWorldAssertion helloWorldAssertion) throws ValidationException {
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
