package net.microfalx.zenith.node;

import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.Resource;
import net.microfalx.resource.SharedResource;
import net.microfalx.zenith.api.common.Session;
import org.openqa.selenium.logging.LoggingPreferences;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Document me
 */
public class NodeUtilities {

    public static long INTERVAL_BETWEEN_SCREENSHOTS = 2000;

    public static String SELENIUM_SHARED_DIRECTORY = "selenium";
    public static Metrics METRICS = Metrics.of("Node");

    /**
     * Returns the project name for a given session.
     *
     * @param session the session
     * @return the project name
     */
    public static String getProjectName(Session session) {
        return StringUtils.defaultIfEmpty(StringUtils.capitalizeWords(session.getProject()), StringUtils.NA_STRING);
    }

    /**
     * Returns capabilities as a string description.
     *
     * @param capabilities the capabilities
     * @return the string representation
     */
    public static String getCapabilitiesDescription(Map<String, Object> capabilities) {
        if (ObjectUtils.isEmpty(capabilities)) return StringUtils.NA_STRING;
        Map<String, String> mapping = new LinkedHashMap<>();
        capabilities.forEach((k, v) -> mapping.put(k, getCapabilityValue(v)));
        return mapping.toString();
    }

    /**
     * Returns the directory which will store run artifacts.
     *
     * @return the directory
     */
    public static Resource getSessionDirectory() {
        return SharedResource.directory(SELENIUM_SHARED_DIRECTORY + "_session");
    }

    /**
     * Sets up the Java logger to be more suitable for Selenium.
     */
    public static void setupJavaLogger() {
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.INFO);
        Logger.getLogger("sun").setLevel(Level.WARNING);
        Logger.getLogger("com.sun").setLevel(Level.WARNING);
        Logger.getLogger("java").setLevel(Level.WARNING);
        Logger.getLogger("javax").setLevel(Level.WARNING);
    }

    /**
     * Converts a capability value to a string representation.
     *
     * @param value the value
     * @return the string representation
     */
    @SuppressWarnings("unchecked")
    private static String getCapabilityValue(Object value) {
        if (value instanceof LoggingPreferences) {
            LoggingPreferences loggingPreferences = (LoggingPreferences) value;
            Map<String, String> values = new LinkedHashMap<>();
            loggingPreferences.getEnabledLogTypes().forEach(type -> values.put(type, ObjectUtils.toString(loggingPreferences.getLevel(type))));
            return getCapabilityValue(values);
        } else if (value instanceof Map) {
            Map<String, String> values = new LinkedHashMap<>();
            ((Map<?, ?>) value).forEach((k, v) -> values.put(ObjectUtils.toString(k), ObjectUtils.toString(v)));
            return getCapabilityValue(values);
        } else {
            return ObjectUtils.toString(value);
        }
    }

    /**
     * Converts a capability value to a string representation.
     *
     * @param values the values
     * @return the string representation
     */
    private static String getCapabilityValue(Map<String, String> values) {
        return ObjectUtils.toString(values);
    }
}
