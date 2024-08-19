package net.microfalx.zenith.api.hub;

import net.microfalx.zenith.api.common.Log;
import net.microfalx.zenith.api.common.Screenshot;
import net.microfalx.zenith.api.common.Session;
import net.microfalx.zenith.api.node.Node;
import net.microfalx.zenith.api.node.Slot;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collection;

/**
 * Document me
 */
@Service
public interface HubService {

    /**
     * Returns the Hub information.
     *
     * @return a non-null instance
     */
    Hub getHub();

    /**
     * Returns the URI of the Selenium HUB.
     *
     * @return a non-null instance
     */
    URI getUri();

    /**
     * Returns the URI of the Selenium HUB web service.
     *
     * @return a non-null instance
     */
    URI getWsUri();

    /**
     * Returns whether the HUB is ready.
     *
     * @return {@code true} if ready, {@code false} otherwise
     */
    boolean isReady();

    /**
     * Returns the active sessions running in this node.
     *
     * @return a non-null instance
     */
    Collection<Session> getSessions();

    /**
     * Returns a session.
     *
     * @param id the session identifier
     * @return a non-null instance
     */
    Session getSession(String id);

    /**
     * Returns the available slots across all nodes.
     *
     * @return a non-null instance
     */
    Collection<Slot> getSlots();

    /**
     * Returns a registered node.
     *
     * @param id the node identifier
     * @return the node
     */
    Node getNode(String id);

    /**
     * Returns the log of a session.
     * <p>
     * If the session does not exist, a log entry will be returned with text "No logs available".
     *
     * @param id   the session identifier
     * @param type the type of log
     * @return the log instance
     */
    Log getLog(String id, Log.Type type);

    /**
     * Returns the screenshot of a session.
     *
     * @param id the session identifier
     * @return a non-null instance
     */
    Screenshot getScreenshot(String id);

    /**
     * Returns whether the session is running.
     *
     * @param id the session identifier
     * @return <code>true</code> if the session is running, <code>false</code> otherwise
     */
    boolean isRunning(String id);

    /**
     * Returns all registered nodes.
     *
     * @return a non-null instance
     */
    Collection<Node> getNodes();

    /**
     * Refreshes caches (nodes, sessions, etc).
     */
    void refresh();
}
