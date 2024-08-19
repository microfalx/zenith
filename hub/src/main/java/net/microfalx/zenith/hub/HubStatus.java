package net.microfalx.zenith.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.zenith.api.hub.Hub;
import net.microfalx.zenith.base.rest.RestClient;
import net.microfalx.zenith.node.NodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which extracts the status of the {@link Hub} over Rest API.
 */
class HubStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(HubStatus.class);

    private final Hub hub;

    private boolean ready;
    private Collection<Node> nodes = Collections.emptyList();
    private String message;

    HubStatus(Hub hub) {
        requireNonNull(hub);
        this.hub = hub;
    }

    public boolean isReady() {
        return ready;
    }

    public Collection<Node> getNodes() {
        return unmodifiableCollection(nodes);
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
                extractNodes(response.value);
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
