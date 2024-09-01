package net.microfalx.zenith.api.common;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.zenith.api.node.Slot;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.setFromString;
import static net.microfalx.lang.StringUtils.NA_STRING;

/**
 * A Selenium session executed by a {@link Project}.
 */
@Getter
@ToString
public class Session extends NamedIdentityAware<String> implements Serializable {

    public static final String CAPABILITY = "zenith:options";

    public static final String LOGGING_CAPABILITY = "loggingPrefs";
    public static final String PROJECT_CAPABILITY = "sessionProject";
    public static final String NAME_CAPABILITY = "sessionName";
    public static final String NAMESPACE_CAPABILITY = "sessionNamespace";
    public static final String CATEGORY_CAPABILITY = "sessionCategory";
    public static final String TAGS_CAPABILITY = "sessionTags";

    public static final String DEFAULT_PROJECT = "Default";

    @Serial
    private static final long serialVersionUID = -4239989160165245108L;

    private Slot slot;
    private Map<String, Object> capabilities;
    private Status status;
    private Reason reason;
    private Browser browser;
    private String browserVersion;
    private String key;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private boolean closed;

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String getProject() {
        return getZenithOption(PROJECT_CAPABILITY, DEFAULT_PROJECT);
    }

    public String getNamespace() {
        return getZenithOption(NAMESPACE_CAPABILITY, NA_STRING);
    }

    public String getCategory() {
        return getZenithOption(CATEGORY_CAPABILITY, NA_STRING);
    }

    public Set<String> getTags() {
        return setFromString(getZenithOption(TAGS_CAPABILITY, null));
    }

    public Map<String, Object> getCapabilities() {
        return unmodifiableMap(capabilities);
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

    public String getBrowserVersion() {
        if (browserVersion == null) {
            browserVersion = StringUtils.defaultIfEmpty((String) capabilities.get("browserVersion"), NA_STRING);
        }
        return browserVersion;
    }

    public Session withSlot(Slot slot) {
        Session copy = copy();
        copy.slot = slot;
        return copy;
    }

    @Override
    protected String dynamicName() {
        return getZenithOption(NAME_CAPABILITY, getId());
    }

    protected Session copy() {
        return (Session) super.copy();
    }

    @SuppressWarnings("unchecked")
    private <T> T getZenithOption(String name, T defaultValue) {
        return (T) getZenithOptions().getOrDefault(name, defaultValue);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getZenithOptions() {
        return (Map<String, Object>) capabilities.getOrDefault("zenith:options", Collections.<String, Object>emptyMap());
    }

    public enum Status {
        CREATED,
        RUNNING,
        SUCCESSFUL,
        KILLED,
        FAILED
    }

    public enum Reason {
        TIMEOUT,
        SOCKET_TIMEOUT,
        BROWSER_TIMEOUT,
        ORPHAN,
        CLIENT_GONE,
        NODE_FAILED,
        NODE_REMOVED,
        CREATION_FAILED,
        REGISTRATION
    }

    public static class Builder extends NamedIdentityAware.Builder<String> {

        private final Map<String, Object> capabilities = new HashMap<>();
        private Status status;
        private Reason reason;
        private Browser browser;
        private String browserVersion;
        private String key;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private boolean closed;

        Builder(String id) {
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

        public Builder browser(Browser browser) {
            this.browser = browser;
            return this;
        }

        public Builder browserVersion(String browserVersion) {
            this.browserVersion = browserVersion;
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
            session.browser = browser;
            session.browserVersion = browserVersion;
            session.key = key;
            session.status = status;
            session.reason = reason;
            session.closed = closed;
            session.startedAt = startedAt;
            session.endedAt = endedAt;
            session.setDescription(extractDescription());
            return session;
        }

        private String extractDescription() {
            StringBuilder builder = new StringBuilder();
            appendIfPresent(builder, "pageLoadStrategy", "Page Load Strategy");
            appendIfPresent(builder, "platformName", "Platform");
            return builder.toString();
        }

        private void appendIfPresent(StringBuilder builder, String name, String title) {
            Object value = capabilities.get(name);
            if (value != null) StringUtils.append(builder, title + ": " + value);
        }
    }
}
