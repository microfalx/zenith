package net.microfalx.zenith.base.grid;

import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.events.EventBus;
import org.openqa.selenium.grid.data.*;
import org.openqa.selenium.grid.sessionmap.local.LocalSessionMap;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.tracing.AttributeKey;
import org.openqa.selenium.remote.tracing.AttributeMap;
import org.openqa.selenium.remote.tracing.Span;
import org.openqa.selenium.remote.tracing.Tracer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static org.openqa.selenium.remote.RemoteTags.SESSION_ID;
import static org.openqa.selenium.remote.RemoteTags.SESSION_ID_EVENT;

public class SessionMap extends org.openqa.selenium.grid.sessionmap.SessionMap {

    private static final Logger LOG = Logger.getLogger(LocalSessionMap.class.getName());

    private final EventBus bus;
    private final Map<SessionId, Session> knownSessions = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock(/* be fair */ true);

    SessionManager sessionManager;

    public SessionMap(Tracer tracer, EventBus bus) {
        super(tracer);

        this.bus = Require.nonNull("Event bus", bus);

        bus.addListener(
                SessionClosedEvent.listener(
                        id -> {
                            try (Span span = tracer.getCurrentContext().createSpan("local_sessionmap.remove")) {
                                AttributeMap attributeMap = tracer.createAttributeMap();
                                attributeMap.put(AttributeKey.LOGGER_CLASS.getKey(), getClass().getName());
                                SESSION_ID.accept(span, id);
                                SESSION_ID_EVENT.accept(attributeMap, id);
                                Session session = knownSessions.get(id);
                                if (session != null) {
                                    doRemove(session, net.microfalx.zenith.api.common.Session.Status.SUCCESSFUL, null);
                                }
                                String sessionDeletedMessage = "Deleted session from local Session Map";
                                span.addEvent(sessionDeletedMessage, attributeMap);
                                LOG.info(String.format("%s, Id: %s", sessionDeletedMessage, id));
                            }
                        }));

        bus.addListener(
                NodeRemovedEvent.listener(
                        nodeStatus ->
                                nodeStatus.getSlots().stream()
                                        .map(Slot::getSession).filter(Objects::nonNull)
                                        .forEach(session -> doRemove(session, net.microfalx.zenith.api.common.Session.Status.FAILED,
                                                net.microfalx.zenith.api.common.Session.Reason.NODE_REMOVED))));

        bus.addListener(
                NodeRestartedEvent.listener(
                        nodeStatus -> knownSessions.values().stream().filter(value -> value.getUri().equals(nodeStatus.getExternalUri()))
                                .forEach(session -> doRemove(session, net.microfalx.zenith.api.common.Session.Status.FAILED,
                                        net.microfalx.zenith.api.common.Session.Reason.NODE_FAILED))));
    }

    @Override
    public boolean isReady() {
        return bus.isReady();
    }

    @Override
    public boolean add(Session session) {
        Require.nonNull("Session", session);
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try (Span span = tracer.getCurrentContext().createSpan("local_sessionmap.add")) {
            AttributeMap attributeMap = tracer.createAttributeMap();
            attributeMap.put(AttributeKey.LOGGER_CLASS.getKey(), getClass().getName());
            SessionId id = session.getId();
            SESSION_ID.accept(span, id);
            SESSION_ID_EVENT.accept(attributeMap, id);
            knownSessions.put(session.getId(), session);
            doAdd(session);
            span.addEvent("Added session into local session map", attributeMap);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Session get(SessionId id) {
        Require.nonNull("Session ID", id);
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            Session session = knownSessions.get(id);
            if (session == null) {
                throw new NoSuchSessionException("Unable to find session with ID: " + id);
            }
            return session;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void remove(SessionId id) {
        Require.nonNull("Session ID", id);
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            Session session = knownSessions.get(id);
            knownSessions.remove(id);
            if (session != null) doRemove(session, net.microfalx.zenith.api.common.Session.Status.KILLED, null);
        } finally {
            writeLock.unlock();
        }
    }

    private void doAdd(Session session) {
        requireNonNull(session);
        if (sessionManager != null) sessionManager.addSession(session);
    }

    private void doRemove(Session session, net.microfalx.zenith.api.common.Session.Status status,
                          net.microfalx.zenith.api.common.Session.Reason reason) {
        requireNonNull(session);
        if (status != net.microfalx.zenith.api.common.Session.Status.KILLED) remove(session.getId());
        if (sessionManager != null) sessionManager.removeSession(session, status, reason);
    }
}
