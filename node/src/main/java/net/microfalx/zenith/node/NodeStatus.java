package net.microfalx.zenith.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.zenith.base.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which extracts the status of the {@link Node} over Rest API.
 */
public class NodeStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeStatus.class);

    private final net.microfalx.zenith.api.node.Node node;

    private boolean ready;
    private Collection<net.microfalx.zenith.api.node.Slot> slots = Collections.emptyList();
    private String message;

    NodeStatus(net.microfalx.zenith.api.node.Node node) {
        requireNonNull(node);
        this.node = node;
    }

    public boolean isReady() {
        return ready;
    }

    public Collection<net.microfalx.zenith.api.node.Slot> getSlots() {
        return unmodifiableCollection(slots);
    }

    public String getMessage() {
        return message;
    }

    public boolean execute() {
        RestClient<Response> client = RestClient.create(node.getStatusUri(), Response.class);
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
            LOGGER.error("Failed to extract node status", e);
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
        private Node node;
    }

    @Getter
    @Setter
    @ToString
    public static class Node {

        @JsonProperty("nodeId")
        private String id;
        @JsonProperty("externalUri")
        private String uri;
        private int maxSessions;
        private OsInfo osInfo;
        private String availability;
        private Collection<Slot> slots;

    }

    @Getter
    @Setter
    @ToString
    public static class OsInfo {

        private String arch;
        private String name;
        private String version;
    }

    @Getter
    @Setter
    @ToString
    public static class Slot {

        private SlotId id;
        @JsonProperty("lastStarted")
        private String lastStartedAt;
        private Session session;

    }

    @Getter
    @Setter
    @ToString
    public static class SlotId {
        private String hostId;
        private String id;
    }

    @Getter
    @Setter
    @ToString
    public static class StereoType {

        private String browserName;
        private String platformName;
    }

    @Getter
    @Setter
    @ToString
    public static class Session {

        @JsonProperty("sessionId")
        private String id;
        @JsonProperty("start")
        private String startedAt;
        @JsonProperty("stereotype")
        private StereoType stereoType;
        private String uri;
        private Map<String, Object> capabilities;
    }

}
