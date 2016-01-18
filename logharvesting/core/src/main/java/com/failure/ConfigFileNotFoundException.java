package com.failure;

/**
 * {@link RuntimeException} that is thrown when a config file cannot be found.
 *
 * @author brad_bottjen
 */
public class ConfigFileNotFoundException extends AttributedRuntimeException {

    private static final long serialVersionUID = -5034999668266586595L;

    /**
     * Default constructor.
     */
    public ConfigFileNotFoundException() {
        super();
    }

    /**
     * Message constructor.
     *
     * @param message The detail message, saved for later retrieval by the {@link #getMessage()} method.
     */
    public ConfigFileNotFoundException(String message, String filePath) {
        super(message);
        setProperty(FILE_PATH, filePath);
    }
}
