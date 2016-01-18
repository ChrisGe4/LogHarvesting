package com.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris_ge
 */
public abstract class AbstractStartable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractStartable.class);

    public abstract void start();

    public abstract void shutdown();

}
