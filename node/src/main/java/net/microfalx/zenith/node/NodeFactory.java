package net.microfalx.zenith.node;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.zenith.api.hub.HubException;
import net.microfalx.zenith.base.grid.Component;
import net.microfalx.zenith.client.DriverManager;
import net.microfalx.zenith.client.Options;
import org.openqa.selenium.grid.node.Node;
import org.openqa.selenium.grid.node.httpd.NodeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static net.microfalx.lang.FormatterUtils.formatDuration;

/**
 * A factory which creates the Selenium Node.
 */
@Setter
@Getter
public class NodeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFactory.class);

    private volatile static NodeFactory instance;

    private volatile Component<NodeServer> component;
    private volatile Node node;

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
        component = Component.create(NodeServer.class)
                .option("selenium-manager", "true");
        component.start();
        LOGGER.info("Selenium Node was started");
    }

    /**
     * Shuts down the server and its components.
     */
    private void stop() {
        LOGGER.info("Shutdown Selenium Node");
        if (component != null) component.stop();
    }


    private void logConfiguration() {
        LOGGER.info(" - port: " + properties.getPort());
        LOGGER.info(" - timeout: " + formatDuration(properties.getTimeout()));
        LOGGER.info(" - browser timeout: " + formatDuration(properties.getBrowserTimeout()));
        LOGGER.info(" - maximum threads: " + properties.getMaxThreads());
        LOGGER.info(" - hub uri: " + hubUri);
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
