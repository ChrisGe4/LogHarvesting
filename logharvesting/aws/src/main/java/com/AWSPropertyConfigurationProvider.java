/*
 * Copyright 2013-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com;

import javax.inject.Inject;
import com.google.inject.Provider;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.config.PropertyConfiguration;

/**
 * This class contains constants used to configure AWS Services in Amazon Kinesis Connectors. The user
 * should use System properties to set their proper configuration. An instance of
 * KinesisConnectorConfiguration is created with System properties and an AWSCredentialsProvider.
 * For example:
 * <p/>
 * <pre>
 * Properties prop = new Properties();
 * prop.put(KinesisConnectorConfiguration.PROP_APP_NAME, &quot;MyKinesisConnector&quot;);
 * KinesisConnectorConfiguration config =
 *         new KinesisConnectorConfiguration(prop, new DefaultAWSCredentialsProviderChain());
 * </pre>
 */
public class AWSPropertyConfigurationProvider implements Provider<AWSPropertyConfiguration> {

    private AWSPropertyConfiguration config;
    private PropertyConfiguration propConfig;
    private Provider<AWSCredentialsProvider> credentialsProvider;

    @Inject
    public AWSPropertyConfigurationProvider(PropertyConfiguration propConfig, Provider<AWSCredentialsProvider> credentialsProvider) {
        this.propConfig = propConfig;
        this.credentialsProvider = credentialsProvider;

    }

    @Override
    public AWSPropertyConfiguration get() {
        if (config == null) {
            config = new AWSPropertyConfiguration(propConfig, credentialsProvider.get());
        }
        return config;
    }
}
