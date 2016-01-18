package com.core;

import javax.inject.Singleton;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.config.PropertiesLoader;
import com.config.PropertyConfiguration;
import com.config.PropertyConfigurationImpl;

/**
 * @author chris_ge
 */
public class CoreServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("project")).toInstance("project.properties");
        bind(String.class).annotatedWith(Kinesis.class).toInstance("kinesis.consumer.properties");

        bind(String.class).annotatedWith(Names.named("kinesis.producer")).toInstance("kinesis.producer.properties");
        bind(String.class).annotatedWith(S3.class).toInstance("s3.properties");
        bind(PropertyConfiguration.class).to(PropertyConfigurationImpl.class).in(Singleton.class);
        bind(PropertiesLoader.class);
    }
}
