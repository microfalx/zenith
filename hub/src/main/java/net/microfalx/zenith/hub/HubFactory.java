package net.microfalx.zenith.hub;

import net.microfalx.zenith.api.common.Server;
import net.microfalx.zenith.api.hub.HubException;
import org.openqa.selenium.grid.commands.Hub;
import org.openqa.selenium.grid.config.Config;
import org.openqa.selenium.grid.config.MapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.FormatterUtils.formatDuration;
import static net.microfalx.zenith.api.hub.Hub.HUB_PATH;

/**
 * A factory which creates the Selenium Hub.
 */
public class HubFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HubFactory.class);

    private volatile static HubFactory instance;

    private volatile Hub hub;
    private volatile org.openqa.selenium.grid.server.Server<?> server;

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

    public Hub getHub() {
        if (hub == null) throw new HubException("A Selenium Hub is not started");
        return hub;
    }

    public org.openqa.selenium.grid.server.Server<?> getServer() {
        if (server == null) throw new HubException("A Selenium Server is not started");
        return server;
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
        hub = new org.openqa.selenium.grid.commands.Hub();
        server = hub.asServer(getConfig());
        LOGGER.info("Selenium Hub was started, url " + server.getUrl());
    }

    /**
     * Shuts down the server and its components.
     */
    private synchronized void stop() {
        LOGGER.info("Shutdown Selenium Hub");
        if (server != null && server.isStarted()) {
            server.stop();
            server = null;
        }
    }

    private void logConfiguration() {
        LOGGER.info(" - port: " + properties.getPort());
        LOGGER.info(" - timeout: " + formatDuration(properties.getTimeout()));
        LOGGER.info(" - browser timeout: " + formatDuration(properties.getBrowserTimeout()));
    }

    private Config getConfig() {
        Map<String, Object> config = new HashMap<>();
        Map<String, Object> eventsConfig = new HashMap<>();
        config.put("events", eventsConfig);
        eventsConfig.put("publish", "tcp://*:" + (properties.getPort() - 2));
        eventsConfig.put("subscribe", "tcp://*:" + (properties.getPort() - 1));
        eventsConfig.put("bind", true);
        Map<String, Object> sessionsConfig = new HashMap<>();
        config.put("sessions", sessionsConfig);
        sessionsConfig.put("implementation", "org.openqa.selenium.grid.sessionmap.local.LocalSessionMap");
        Map<String, Object> serverConfig = new HashMap<>();
        config.put("server", serverConfig);
        serverConfig.put("port", properties.getPort());
        serverConfig.put("max-threads", properties.getMaxThreads());
        return new MapConfig(config);
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
