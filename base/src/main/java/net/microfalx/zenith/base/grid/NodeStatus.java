package net.microfalx.zenith.base.grid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.UriUtils;
import net.microfalx.zenith.api.common.Browser;
import net.microfalx.zenith.base.ZenithUtils;
import net.microfalx.zenith.base.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private net.microfalx.zenith.api.node.Node seleniumNode;
    private Collection<net.microfalx.zenith.api.node.Slot> slots = Collections.emptyList();
    private String message;

    public NodeStatus(net.microfalx.zenith.api.node.Node node) {
        requireNonNull(node);
        this.node = node;
    }

    public boolean isReady() {
        return ready;
    }

    public net.microfalx.zenith.api.node.Node getNode() {
        return seleniumNode != null ? seleniumNode : node;
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
                extractNode(response.value);
                extractSlots(response.value);
            } else {
                message = "No status available";
            }
        } catch (Exception e) {
            message = ExceptionUtils.getRootCauseMessage(e);
            LOGGER.error("Failed to extract node status", e);
        }
        return ready;
    }

    public static net.microfalx.zenith.api.node.Slot from(net.microfalx.zenith.api.node.Node node, Slot slot) {
        requireNonNull(node);
        requireNonNull(slot);
        net.microfalx.zenith.api.node.Slot.Builder builder = net.microfalx.zenith.api.node.Slot.builder(slot.getId().getId(), node);
        if (slot.getStereoType() != null) {
            builder.browser(Browser.from(slot.getStereoType().getBrowserName()));
        }
        if (slot.getSession() != null) {
            builder.session(from(node, slot.getSession()));
        }
        return builder.build();
    }

    public static net.microfalx.zenith.api.common.Session from(net.microfalx.zenith.api.node.Node node, Session session) {
        requireNonNull(node);
        requireNonNull(session);
        net.microfalx.zenith.api.common.Session.Builder builder = net.microfalx.zenith.api.common.Session.builder(session.getId());
        builder.capabilities(session.getCapabilities())
                .status(net.microfalx.zenith.api.common.Session.Status.RUNNING);
        if (session.getStereoType() != null) {
            builder.browser(Browser.from(session.getStereoType().getBrowserName()));
        }
        try {
            ZonedDateTime startTime = ZonedDateTime.parse(session.startedAt, DateTimeFormatter.ISO_DATE_TIME)
                    .withZoneSameInstant(ZoneId.systemDefault());
            builder.time(startTime.toLocalDateTime(), null);
        } catch (Exception e) {
            builder.time(LocalDateTime.now(), null);
        }
        return builder.build();
    }

    private void extractStatus(Status status) {
        this.ready = status.ready;
        this.message = status.message;
    }

    private void extractNode(Status status) {
        Node responseNode = status.node;
        URI uri = UriUtils.parseUri(responseNode.uri);
        net.microfalx.zenith.api.node.Node.Builder builder = net.microfalx.zenith.api.node.Node.builder(uri);
        builder.maxSessions(responseNode.getMaxSessions())
                .state(ZenithUtils.parseState(responseNode.availability))
                .id(responseNode.getId());
        this.seleniumNode = builder.build();
    }

    private void extractSlots(Status status) {
        slots = new ArrayList<>();
        for (Slot slot : status.node.slots) {
            slots.add(from(getNode(), slot));
        }
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
        @JsonProperty("stereotype")
        private StereoType stereoType;
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
