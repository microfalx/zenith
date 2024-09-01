package net.microfalx.zenith.base.grid;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.zenith.api.common.Session;
import net.microfalx.zenith.api.hub.Hub;
import net.microfalx.zenith.api.node.Slot;
import net.microfalx.zenith.base.ZenithUtils;
import net.microfalx.zenith.base.rest.RestClient;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.UriUtils.parseUri;

/**
 * A class which extracts the status of the {@link Hub} over Rest API.
 */
public class HubStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(HubStatus.class);

    private final Hub hub;
    private final boolean statusOnly;

    private boolean ready;
    private Collection<net.microfalx.zenith.api.node.Node> nodes = Collections.emptyList();
    private Collection<Slot> slots = Collections.emptyList();
    private Collection<Session> sessions = Collections.emptyList();
    private Map<SessionId, Node> sessionIdNodes = Collections.emptyMap();
    private String message;

    public HubStatus(Hub hub, boolean statusOnly) {
        requireNonNull(hub);
        this.hub = hub;
        this.statusOnly = statusOnly;
    }

    public boolean isReady() {
        return ready;
    }

    public Collection<net.microfalx.zenith.api.node.Node> getNodes() {
        return unmodifiableCollection(nodes);
    }

    public Collection<Slot> getSlots() {
        return unmodifiableCollection(slots);
    }

    public Collection<Session> getSessions() {
        return unmodifiableCollection(sessions);
    }

    public Map<SessionId, Node> getSessionIdNodes() {
        return unmodifiableMap(sessionIdNodes);
    }

    public String getMessage() {
        return message;
    }

    public boolean execute() {
        RestClient<Response> client = RestClient.create(hub.getStatusUri(), Response.class);
        try {
            Response response = client.execute();
            if (response != null && response.value != null) {
                extractStatus(response.value);
                if (!statusOnly) extractNodes(response.value);
            } else {
                message = "No status available";
            }
        } catch (Exception e) {
            message = ExceptionUtils.getRootCauseMessage(e);
            LOGGER.error("Failed to extract hub status", e);
        }
        return ready;
    }

    private void extractStatus(Status status) {
        this.ready = status.ready;
        this.message = status.message;
    }

    private void extractNodes(Status status) {
        nodes = new ArrayList<>();
        slots = new ArrayList<>();
        sessions = new ArrayList<>();
        sessionIdNodes = new HashMap<>();
        for (Node node : status.nodes) {
            nodes.add(extractNode(node));
        }
    }

    private net.microfalx.zenith.api.node.Node extractNode(Node node) {
        net.microfalx.zenith.api.node.Node.Builder builder = net.microfalx.zenith.api.node.Node.builder(parseUri(node.getUri()));
        builder.state(ZenithUtils.parseState(node.getAvailability()))
                .maxSessions(node.getMaxSessions());
        net.microfalx.zenith.api.node.Node zenithNode = builder.build();
        for (NodeStatus.Slot slot : node.getSlots()) {
            Slot zenithSlot = NodeStatus.from(zenithNode, slot);
            slots.add(zenithSlot);
            if (zenithSlot.getSession() != null) {
                sessions.add(zenithSlot.getSession());
                sessionIdNodes.put(new SessionId(slot.getSession().getId()), node);
            }
        }
        return zenithNode;
    }

    @Getter
    @Setter
    @ToString
    public static class Response {

        private Status value;

    }

    @Getter
    @Setter
    @ToString
    public static class Status {

        private boolean ready;
        private String message;
        private Collection<Node> nodes;
    }

    @Getter
    @Setter
    @ToString
    public static class Node {

        private String id;
        private String uri;
        private int maxSessions;
        private NodeStatus.OsInfo osInfo;
        private String availability;
        private Collection<NodeStatus.Slot> slots;

    }

}
