package com.failure;

import java.util.Map;
import com.google.common.collect.Maps;

/**
 * @author chris_ge
 */
public abstract class AttributedRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Map<String,String> attributes = Maps.newHashMap();
    String FILE_PATH = "filePath";
    String FILE_NAME = "fileName";
    String ACTION_IDENTIFIER = "actionIdentifier";
    String ROOT_CAUSE = "rootCause";

    protected AttributedRuntimeException() {
        super();
    }

    /**
     * Message+Cause constructor.
     *
     * @param message The message of this Exception.
     * @param cause   The cause of this Exception.
     */
    protected AttributedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Message constructor.
     *
     * @param message Default constructor.
     */
    protected AttributedRuntimeException(String message) {
        super(message);
    }

    /**
     * Cause constructor.
     *
     * @param cause The cause of this Exception.
     */
    protected AttributedRuntimeException(Throwable cause) {
        super(cause);
    }

    public void setProperty(String key, String value) {
        this.attributes.put(key, value);
    }

    public Map<String,String> getAttributes() {
        return this.attributes;
    }

}
