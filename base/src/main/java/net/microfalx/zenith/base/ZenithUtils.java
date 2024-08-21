package net.microfalx.zenith.base;

import net.microfalx.metrics.Metrics;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.SharedResource;
import net.microfalx.zenith.api.node.Node;

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

    /**
     * Parses the node state from its string representation (based on Selenium Rest API response).
     *
     * @param value the state as string
     * @return a non-null enum
     */
    public static Node.State parseState(String value) {
        if (value == null) return Node.State.INIT;
        value = value.toUpperCase();
        return switch (value) {
            case "UP" -> Node.State.UP;
            case "DOWN" -> Node.State.DOWN;
            default -> Node.State.INIT;
        };
    }
}
