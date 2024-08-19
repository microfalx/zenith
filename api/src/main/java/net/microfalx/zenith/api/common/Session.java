package net.microfalx.zenith.api.common;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.*;
import net.microfalx.zenith.api.node.Slot;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.*;

/**
 * A Selenium session executed by a {@link Project}.
 */
@Getter
@ToString
public class Session extends NamedIdentityAware<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = -4239989160165245108L;

    private Slot slot;
    private Map<String, Object> capabilities;
    private Status status;
    private Reason reason;
    private Browser browser;
    private String key;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private boolean closed;

    public Map<String, Object> getCapabilities() {
        return unmodifiableMap(capabilities);
    }

    public Set<String> getTags() {
        return CollectionUtils.setFromString((String) capabilities.get("sessionTags"));
    }

    public String getPackage() {
        return (String) capabilities.get("sessionPackage");
    }

    public String getProject() {
        return defaultIfEmpty((String) capabilities.get("sessionProject"), EMPTY_STRING);
    }

    public String getCategory() {
        return (String) capabilities.get("sessionCategory");
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public Duration getDuration() {
        return endedAt != null ? Duration.between(startedAt, endedAt) : Duration.between(startedAt, LocalDateTime.now());
    }

    public Browser getBrowser() {
        if (browser == null) {
            browser = EnumUtils.fromName(Browser.class, (String) capabilities.get("browserName"), Browser.OTHER);
        }
        return browser;
    }

    public Session withSlot(Slot slot) {
        Session copy = copy();
        copy.slot = slot;
        return copy;
    }

    @Override
    protected String dynamicName() {
        if (StringUtils.isEmpty(super.getName())) {
            setName((String) capabilities.get("sessionName"));
        }
        return defaultIfEmpty(getName(), NA_STRING);
    }

    protected Session copy() {
        return (Session) super.copy();
    }

    public enum Status {
        CREATED,
        RUNNING,
        SUCCESSFUL,
        FAILED
    }

    public enum Reason {
        TIMEOUT,
        SOCKET_TIMEOUT,
        BROWSER_TIMEOUT,
        ORPHAN,
        CLIENT_GONE,
        NODE_FAILED,
        CREATION_FAILED,
        REGISTRATION
    }

    public static class Builder extends NamedIdentityAware.Builder<String> {

        private final Map<String, Object> capabilities = new HashMap<>();
        private Status status;
        private Reason reason;
        private String key;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private boolean closed;

        public Builder(String id) {
            super(id);
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder capability(String name, Object value) {
            requireNonNull(name);
            capabilities.put(name, value);
            return this;
        }

        public Builder capabilities(Map<String, Object> values) {
            if (values != null) capabilities.putAll(values);
            return this;
        }

        public Builder status(Status status) {
            requireNonNull(status);
            this.status = status;
            return this;
        }

        public Builder reason(Reason reason) {
            requireNonNull(reason);
            this.reason = reason;
            return this;
        }

        public Builder time(LocalDateTime startedAt, LocalDateTime endedAt) {
            requireNonNull(startedAt);
            this.startedAt = startedAt;
            this.endedAt = endedAt;
            return this;
        }

        public Builder closed(boolean closed) {
            this.closed = closed;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Session();
        }

        public Session build() {
            Session session = (Session) super.build();
            session.capabilities = capabilities;
            session.key = key;
            session.status = status;
            session.reason = reason;
            session.closed = closed;
            session.startedAt = startedAt;
            session.endedAt = endedAt;
            return session;
        }
    }
}
