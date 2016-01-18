package com.config;

import java.util.Properties;
import com.google.inject.ImplementedBy;

/**
 * @author chris_ge
 */
@ImplementedBy(PropertyConfigurationImpl.class)
public interface PropertyConfiguration {

    public Properties getSetupProperties();

    public Properties getProjectSetupProperties();

    public Properties getAllStartableServices();

    public Properties getAllProperties();

    public Properties getProperties(String type);
}
