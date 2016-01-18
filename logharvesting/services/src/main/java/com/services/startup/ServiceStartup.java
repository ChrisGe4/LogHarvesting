package com.services.startup;

import java.util.Properties;
import com.google.inject.Inject;
import com.config.PropertyConfiguration;
import com.core.AbstractStartable;
import com.kinesis.consumer.KinesisExecutor;

/**
 * Instantiate the Guice binding
 *
 * @author chris_ge
 */
public class ServiceStartup extends AbstractStartable {

    private final PropertyConfiguration propertyConfig;
    // private final AWSPropertyConfiguration awsConfig;
    private KinesisExecutor executor;

    @Inject
    public ServiceStartup(PropertyConfiguration propertyConfig, KinesisExecutor executor
    ) {
        this.propertyConfig = propertyConfig;
        this.executor = executor;
    }

    @Override
    public void start() {

        Properties properties = propertyConfig.getProjectSetupProperties();

        if (properties.getProperty("aws.kinesis.start", "false").equals("true")) {
            executor.initialize();
            executor.run();
        }

    }

    @Override
    public void shutdown() {

    }

}
