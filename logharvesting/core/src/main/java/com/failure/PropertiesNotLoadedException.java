package com.failure;

/**
 * {@link RuntimeException} that is thrown when a config file cannot be found.
 *
 * @author brad_bottjen
 */
public class PropertiesNotLoadedException extends AttributedRuntimeException {

    private static final long serialVersionUID = -5034999668266586596L;

    /**
     * Default constructor.
     */
    public PropertiesNotLoadedException() {
        super();
    }

    /**
     * Message constructor.
     *
     * @param message The detail message, saved for later retrieval by the {@link #getMessage()} method.
     */
    public PropertiesNotLoadedException(String message, String fileName) {
        super(message);
        setProperty(FILE_NAME, fileName);
    }
}
