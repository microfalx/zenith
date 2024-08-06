package net.microfalx.zenith.node;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.zenith.api.hub.HubException;
import net.microfalx.zenith.client.DriverManager;
import net.microfalx.zenith.client.Options;
import org.openqa.selenium.grid.config.Config;
import org.openqa.selenium.grid.config.MapConfig;
import org.openqa.selenium.grid.node.Node;
import org.openqa.selenium.grid.node.local.LocalNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.FormatterUtils.formatDuration;

/**
 * A factory which creates the Selenium Node.
 */
@Setter
@Getter
public class NodeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFactory.class);

    private volatile static NodeFactory instance;

    private volatile Node node;
    private volatile org.openqa.selenium.grid.server.Server<?> server;

    private NodeProperties properties = new NodeProperties();
    private URI hubUri = Options.DEFAULT_URI;

    /**
     * Create and returns a Selenium Node.
     *
     * @return a non-null instance
     */
    public static NodeFactory getInstance() {
        if (instance == null) {
            synchronized (NodeFactory.class) {
                if (instance == null) {
                    instance = new NodeFactory();
                }
            }
        }
        return instance;
    }

    /**
     * Stops the Selenium Grid.
     */
    public static void shutdown() {
        if (instance != null) {
            synchronized (NodeFactory.class) {
                if (instance != null) {
                    instance.stop();
                    instance = null;
                }
            }
        }
    }

    private NodeFactory() {
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    public Node getNode() {
        if (node == null) throw new HubException("A Selenium Node was not started");
        return node;
    }

    /**
     * Creates the server.
     */
    public void startup() {
        DriverManager.getInstance();
        LOGGER.info("Create Selenium Node");
        logConfiguration();
        node = LocalNodeFactory.create(getConfig());
        LOGGER.info("Selenium Node was started, version " + node.getNodeVersion());
    }

    /**
     * Shuts down the server and its components.
     */
    private void stop() {
        LOGGER.info("Shutdown Selenium Node");
        if (server != null) server.stop();
    }


    private void logConfiguration() {
        LOGGER.info(" - port: " + properties.getPort());
        LOGGER.info(" - timeout: " + formatDuration(properties.getTimeout()));
        LOGGER.info(" - browser timeout: " + formatDuration(properties.getBrowserTimeout()));
        LOGGER.info(" - hub uri: " + hubUri);
    }

    private Config getConfig() {
        Map<String, Object> config = new HashMap<>();
        Map<String, Object> nodeConfig = new HashMap<>();
        nodeConfig.put("hub", hubUri);
        Map<String, Object> serverConfig = new HashMap<>();
        Map<String, Object> eventsConfig = new HashMap<>();
        config.put("events", eventsConfig);
        eventsConfig.put("publish", "tcp://*:" + (properties.getPort() - 2));
        eventsConfig.put("subscribe", "tcp://*:" + (properties.getPort() - 1));
        eventsConfig.put("bind", true);
        config.put("server", serverConfig);
        serverConfig.put("port", properties.getPort());
        serverConfig.put("max-threads", properties.getMaxThreads());
        return new MapConfig(config);
    }

    static class ShutdownThread extends Thread {

        public ShutdownThread() {
            setName("Node Shutdown");
        }

        @Override
        public void run() {
            NodeFactory.shutdown();
        }
    }
}
