package com.config;

import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import com.google.common.base.Optional;

/**
 * @author chris_ge
 */
public class PropertyConfigurationImpl implements PropertyConfiguration {
    private final PropertiesLoader loader;
    private Properties projectSetupProperties;
    private Properties allProperties;
    private Properties allStartableServices;

    @Inject
    public PropertyConfigurationImpl(PropertiesLoader loader) {
        this.loader = loader;

    }

    public void reload() {
        /*allProperties = new Properties();
        allStartableServices = new Properties();
        projectSetupProperties = new Properties();*/
        allProperties = null;
        allStartableServices = null;
        projectSetupProperties = null;
        loader.reload();
    }

    @Override
    public Properties getSetupProperties() {
        return null;
    }

    @Override
    public Properties getProjectSetupProperties() {
        if (projectSetupProperties != null)
            return projectSetupProperties;
        Optional<Properties> o = loader.getProjectSetupProperties();
        if (o.isPresent())
            projectSetupProperties = o.get();
        else
            projectSetupProperties = new Properties();
        return projectSetupProperties;
    }

    @Override
    public Properties getAllStartableServices() {
        if (allStartableServices != null)
            return allStartableServices;
        if (projectSetupProperties != null)
            projectSetupProperties = getProjectSetupProperties();

        allStartableServices = new Properties();
        for (String name : projectSetupProperties.stringPropertyNames()) {
            if (projectSetupProperties.getProperty(name).equals("true"))
                allStartableServices.setProperty(name, projectSetupProperties.getProperty(name));
        }
        return allStartableServices;
    }

    @Override
    public Properties getAllProperties() {
        if (allProperties != null)
            return allProperties;

        allProperties = new Properties();

        for (Properties p : loader.getProperties().values()) {
            for (String name : p.stringPropertyNames()) {
                allProperties.setProperty(name, p.getProperty(name));
            }
        }

        return allProperties;
    }

    @Override
    public Properties getProperties(String type) {
        Map<String,Properties> map = loader.getProperties();

        if (map.containsKey(type))
            return map.get(type);
        return new Properties();

    }

}
