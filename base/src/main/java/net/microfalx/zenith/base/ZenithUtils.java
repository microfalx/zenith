package net.microfalx.zenith.base;

import net.microfalx.metrics.Metrics;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.SharedResource;

import java.io.File;

/**
 * Varies utilities around Zenith.
 */
public class ZenithUtils {

    public static final String DIRECTORY_NAME = ".zenith";
    public static Metrics ZENITH_METRICS=Metrics.of("Zenith");



    /**
     * Returns a directory to be used as local storage.
     *
     * @return a non-null instance
     */
    public static Resource getLocalStorage() {
        File userDir = new File(System.getProperty("user.dir"));
        return FileResource.directory(new File(userDir, DIRECTORY_NAME));
    }

    /**
     * Returns a directory to be used as shared storage.
     *
     * @return a non-null instance
     */
    public static Resource getSharedStorage() {
        return SharedResource.directory("zenith");
    }
}
