package com.config;

import static com.config.ServiceType.KINESIS;
import static com.config.ServiceType.PROJECT;
import static com.config.ServiceType.S3;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.inject.name.Named;
import com.core.Kinesis;
import com.core.S3;
import com.failure.ConfigFileNotFoundException;
import com.failure.PropertiesNotLoadedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris_ge
 */
@Singleton
public class PropertiesLoader {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

    private Map<String,Properties> properties;
    //private Map<String,Boolean> allStartupServices;

    private String projectPropertyFile;

    private String kinesisPropertyFile;

    private String s3PropertyFile;

    @Inject
    public PropertiesLoader(@Named("project") String projectPropertyFile, @Kinesis
    String kinesisPropertyFile, @S3 String s3PropertyFile) {
        this.projectPropertyFile = projectPropertyFile;
        this.kinesisPropertyFile = kinesisPropertyFile;
        this.s3PropertyFile = s3PropertyFile;
        reload();
    }

    public void reload() {
        properties = Maps.newHashMap();
        load(PROJECT, projectPropertyFile);
        load(KINESIS, kinesisPropertyFile);
        load(S3, s3PropertyFile);

    }

    private void load(ServiceType type, String configFile) {

        /*
         From ClassLoader, all paths are "absolute" already - there's no context
        from which they could be relative. Therefore you don't need a leading slash.
        InputStream in = this.getClass().getClassLoader()
                                .getResourceAsStream("SomeTextFile.txt");
        From Class, the path is relative to the package of the class unless
        you include a leading slash, so if you don't want to use the current
         package, include a slash like this:
        InputStream in = this.getClass().getResourceAsStream("/SomeTextFile.txt");
         */
        InputStream configStream = this.getClass().getClassLoader().getResourceAsStream("config/" + configFile);
        if (configStream == null) {
            throw new ConfigFileNotFoundException("could not find " + configFile, "need to find a way to give the path");
        }
        Properties prop = new Properties();
        try {
            prop.load(configStream);
            configStream.close();
        } catch (IOException e) {

            String msg = "Could not load properties file " + configFile + " from classpath";
            throw new PropertiesNotLoadedException(msg, configFile);
        }

        properties.put(type.getType(), prop);
    }

    public Optional<Properties> getProjectSetupProperties() {

        if (!properties.containsKey(PROJECT.getType())) return Optional.absent();

        return Optional.of(properties.get(PROJECT.getType()));

    }

    public Map<String,Properties> getProperties() {
        return properties;
    }
}
