package com.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Resources.getResource;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * @author chris_ge
 */
public class URLs {
    private final static String SLASH = File.separator;
    private final static String CURRENT_WORKING_DIRECTORY = System.getProperty("user.dir");

    public static File asFile(URL url) {
        checkNotNull(url);
        File result;
        try {
            result = new File(url.toURI());
        } catch (URISyntaxException e) {
            result = new File(url.getPath());
        }
        return result;
    }

    public static URL toParent(URL url) {

        checkNotNull(url);
        URL res = null;

        try {
            URI uri = url.toURI();
            URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
            res = parent.toURL();
        } catch (Throwable e) {
            // if it's a RuntimeException or Error, re-throw it...
            Throwables.propagateIfPossible(e);
        }
        return res;
    }

    public static URL getRootRelativeTo(Class<?> referenceClass) {

        //String fileName = referenceClass.getCanonicalName();
        // int dotIndex = fileName.lastIndexOf('.');
        //fileName = (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
        String fileName = referenceClass.getSimpleName();
        URL absolute = getResource(referenceClass, fileName + ".class");
        return toParent(absolute);
    }

    public static URL getDirectory(String path) {
        URL url = null;
        if (!Strings.isNullOrEmpty(path)) {
            path = CURRENT_WORKING_DIRECTORY + SLASH + path;
        } else {
            path = CURRENT_WORKING_DIRECTORY;
        }
        try {
            url = new File(path).toURI().toURL();
        } catch (Throwable e) {
            //logger.error("Could not create URL for current working directory: " + path, e);
            Throwables.propagateIfPossible(e);
        }

        return url;
    }

}
