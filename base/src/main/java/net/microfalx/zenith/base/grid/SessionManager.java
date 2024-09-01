package net.microfalx.zenith.base.grid;

import net.microfalx.zenith.api.node.Node;
import net.microfalx.zenith.base.jpa.SessionRepository;
import org.openqa.selenium.grid.data.Session;
import org.openqa.selenium.grid.distributor.local.LocalDistributor;
import org.openqa.selenium.remote.SessionId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which manages sessions created by Selenium Hub.
 */
@Component
public class SessionManager {

    private final Map<SessionId, Session> sessions = new ConcurrentHashMap<>();
    private final Map<SessionId, Node> sessionIdNode = new ConcurrentHashMap<>();

    LocalDistributor distributor;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private SessionRepository sessionRepository;

    void addSession(Session session) {
        requireNonNull(session);
        sessions.put(session.getId(), session);
    }

    void removeSession(Session session, final net.microfalx.zenith.api.common.Session.Status status,
                       final net.microfalx.zenith.api.common.Session.Reason reason) {
        requireNonNull(session);
        requireNonNull(status);
        sessions.remove(session.getId());
        sessionIdNode.remove(session.getId());
    }

    private class SessionTask implements Runnable {

        private final Session session;
        private final net.microfalx.zenith.api.common.Session.Status status;
        private final net.microfalx.zenith.api.common.Session.Reason reason;

        private SessionTask(Session session, net.microfalx.zenith.api.common.Session.Status status, net.microfalx.zenith.api.common.Session.Reason reason) {
            requireNonNull(session);
            requireNonNull(status);
            this.session = session;
            this.status = status;
            this.reason = reason;
        }

        @Override
        public void run() {

        }
    }
}
