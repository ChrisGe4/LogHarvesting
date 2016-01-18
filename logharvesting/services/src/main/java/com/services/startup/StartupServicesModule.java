package com.services.startup;

import com.google.inject.AbstractModule;
import com.AWSModule;
import com.core.CoreServicesModule;

/**
 * @author chris_ge
 */
public class StartupServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new AWSModule());
        install(new CoreServicesModule());
    }

}
