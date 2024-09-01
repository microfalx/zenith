package net.microfalx.zenith.hub;

import net.microfalx.zenith.api.common.Server;
import net.microfalx.zenith.api.hub.HubException;
import net.microfalx.zenith.base.grid.Component;
import net.microfalx.zenith.base.grid.Hub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static net.microfalx.lang.FormatterUtils.formatDuration;
import static net.microfalx.zenith.api.hub.Hub.HUB_PATH;

/**
 * A factory which creates the Selenium Hub.
 */
public class HubFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HubFactory.class);

    private volatile static HubFactory instance;

    private volatile Component<Hub> component;
    private volatile Hub hub;

    private HubProperties properties = new HubProperties();

    /**
     * Create and returns a Selenium Hub.
     *
     * @return a non-null instance
     */
    public static HubFactory getInstance() {
        if (instance == null) {
            synchronized (HubFactory.class) {
                if (instance == null) {
                    instance = new HubFactory();
                }
            }
        }
        return instance;
    }

    /**
     * Stops the Selenium Hub.
     */
    public static void shutdown() {
        if (instance != null) {
            synchronized (HubFactory.class) {
                if (instance != null) {
                    instance.stop();
                    instance = null;
                }
            }
        }
    }

    /**
     * Returns whether the Selenium hub was started.
     *
     * @return <code>true</code> if started, <code>false</code> otherwise
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    private HubFactory() {
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    /**
     * Returns the extended (our stuff) Selenium Hub.
     *
     * @return a non-null instance
     */
    public Hub getHub() {
        if (hub == null) throw new HubException("A Selenium Hub is not started");
        return hub;
    }

    public HubProperties getProperties() {
        return properties;
    }

    public void setProperties(HubProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns the URI to the Selenium HUB
     *
     * @return a non-null instance
     */
    public URI getUri() {
        return URI.create("http://" + Server.get().getHostname() + ":" + properties.getPort());
    }

    /**
     * Returns the URI to the Selenium HUB web service.
     *
     * @return a non-null instance
     */
    public URI getWsUri() {
        return URI.create(getUri().toASCIIString() + HUB_PATH);
    }

    /**
     * Creates the server.
     */
    public synchronized void start() {
        LOGGER.info("Create Selenium Hub");
        logConfiguration();
        component = Component.create(Hub.class);
        component.start();
        hub = component.get();
        LOGGER.info("Selenium Hub was started");
    }

    /**
     * Shuts down the server and its components.
     */
    private synchronized void stop() {
        LOGGER.info("Shutdown Selenium Hub");
        component.stop();
    }

    private void logConfiguration() {
        LOGGER.info(" - port: " + properties.getPort());
        LOGGER.info(" - timeout: " + formatDuration(properties.getTimeout()));
        LOGGER.info(" - browser timeout: " + formatDuration(properties.getBrowserTimeout()));
    }

    static class ShutdownThread extends Thread {

        public ShutdownThread() {
            setName("Hub Shutdown");
        }

        @Override
        public void run() {
            HubFactory.shutdown();
        }
    }
}
