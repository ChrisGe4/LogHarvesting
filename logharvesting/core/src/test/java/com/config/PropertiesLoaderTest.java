package com.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.base.Throwables.propagateIfPossible;
import static com.google.common.io.Resources.getResource;
import java.io.File;
import java.net.URI;
import java.net.URL;
import org.junit.Test;

/**
 * @author chris_ge
 */
//@RunWith(GuiceJUnitRunner.class)
//@Modules({AWSModule.class})

public class PropertiesLoaderTest {

    //  @Inject
    //@Named("producer")
    // private String fileName;
    @Test
    public void testPath() {
        //  System.out.println("property file ="+fileName);
        System.out.println();
        System.out.println("sys path = " + System.getProperty("user.dir"));
        System.out.println();
        URL root = getRootRelativeTo(PropertiesLoader.class);
        String expectedPath = root.getPath();
        System.out.println("expectedPath = " + expectedPath);
        File file = new File("resources/.");
        System.out.println("file.getPath() = " + file.getPath());
        String classPath = System.getProperty("java.class.path", ".");
        // System.out.println("classPath = " + classPath);
    }

    private URL getRootRelativeTo(Class<?> referenceClass) {

        String fileName = referenceClass.getSimpleName();
        URL absolute = getResource(referenceClass, fileName + ".class");
        return toParent(absolute);
    }

    private URL toParent(URL url) {
        checkNotNull(url);
        URL result = null;
        try {
            URI uri = url.toURI();
            URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
            result = parent.toURL();
        } catch (Throwable t) {
            propagateIfPossible(t); // if it's a RuntimeException or Error, re-throw it...
            propagate(t); // ...otherwise wrap it in a RuntimeException and throw
        }
        return result;
    }
}
