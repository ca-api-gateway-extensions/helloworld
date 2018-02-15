/*
 * Copyright (c) 2017 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.l7tech.external.assertions.helloworld;

import com.l7tech.policy.assertion.Assertion;
import com.l7tech.policy.assertion.AssertionMetadata;
import com.l7tech.policy.assertion.DefaultAssertionMetadata;
import com.l7tech.policy.assertion.UsesVariables;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello World : Example modular assertion for the CA API Gateway
 */
public class HelloWorldAssertion extends Assertion implements UsesVariables {

    public String[] getVariablesUsed() {
        return new String[0];
    }

    private static final String META_INITIALIZED = HelloWorldAssertion.class.getName() + ".metadataInitialized";
    private static final String BASE_NAMESPACE = "com.l7tech.external.assertions.helloworld";

    @Override
    public AssertionMetadata meta() {
        final DefaultAssertionMetadata meta = super.defaultMeta();
        if (Boolean.TRUE.equals(meta.get(META_INITIALIZED)))
            return meta;

        // Cluster properties used by this assertion
        final Map<String, String[]> props = new HashMap<>();
        meta.put(AssertionMetadata.CLUSTER_PROPERTIES, props);

        // Set description for GUI
        meta.put(AssertionMetadata.SHORT_NAME, HelloWorldConstants.MODULE_IDENTITY_FRIENDLY_TAG);
        meta.put(AssertionMetadata.LONG_NAME, HelloWorldConstants.MODULE_IDENTITY_FRIENDLY_TAG);
        meta.put(AssertionMetadata.DESCRIPTION, "This is an example modular assertion for the CA API Gateway");

        // Add to palette folder(s) 
        //   accessControl, transportLayerSecurity, xmlSecurity, xml, routing, 
        //   misc, audit, policyLogic, threatProtection 
        meta.put(AssertionMetadata.PALETTE_FOLDERS, new String[]{"policyLogic"});
        meta.put(AssertionMetadata.PALETTE_NODE_ICON, "com/l7tech/console/resources/MessageLength-16x16.gif");

        // Enable automatic policy advice (default is no advice unless a matching Advice subclass exists)
        meta.put(AssertionMetadata.POLICY_ADVICE_CLASSNAME, "auto");

        // Specify the properties editor dialog class
        meta.put(AssertionMetadata.PROPERTIES_EDITOR_CLASSNAME, BASE_NAMESPACE + ".console.HelloWorldAssertionPropertiesDialog");

        // Wire up the Module Load Listener here. It gets invoked when the module is loaded.
        meta.put(AssertionMetadata.MODULE_LOAD_LISTENER_CLASSNAME, BASE_NAMESPACE + ".server.HelloWorldModuleLoadListener");

        // Set up smart Getter for nice, informative policy node name, for GUI
        meta.put(AssertionMetadata.POLICY_NODE_ICON, "com/l7tech/console/resources/MessageLength-16x16.gif");

        // request default feature set name for our class name, since we are a known optional module
        // that is, we want our required feature set to be "assertion:HelloWorld" rather than "set:modularAssertions"
        meta.put(AssertionMetadata.FEATURE_SET_NAME, "set:modularAssertions");

        meta.put(META_INITIALIZED, Boolean.TRUE);
        return meta;
    }

}
