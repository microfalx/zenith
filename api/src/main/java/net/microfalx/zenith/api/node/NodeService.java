package net.microfalx.zenith.api.node;

import net.microfalx.zenith.api.common.Log;
import net.microfalx.zenith.api.common.Screenshot;
import net.microfalx.zenith.api.common.Session;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collection;

/**
 * The node service.
 */
@Service
public interface NodeService {

    /**
     * Returns node information.
     *
     * @return a non-null instance
     */
    Node getNode();

    /**
     * Returns the URI of the Selenium Node.
     *
     * @return a non-null instance
     */
    URI getUri();

    /**
     * Returns whether the node is ready.
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
     * Returns the screen shot of a session.
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
     * Returns the runners.
     *
     * @return a non-null instance
     */
    Collection<Runner> getRunners();
}
