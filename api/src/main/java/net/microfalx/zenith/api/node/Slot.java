package net.microfalx.zenith.api.node;

import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.zenith.api.common.Browser;
import net.microfalx.zenith.api.common.Session;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A slot in the node.
 */
public class Slot extends NamedIdentityAware<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = -7833149150526840056L;

    private final Node node;

    private Map<String, Object> capabilities;
    private Browser browser;
    private String browserVersion;

    private State state;
    private Session session;

    public static Builder builder(String id, Node node) {
        return new Builder(id, node);
    }

    private Slot(Node node) {
        requireNonNull(node);
        this.setName("Slot ");
        this.setDescription("Node " + node.getName() + ", " + getName());
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public Browser getBrowser() {
        if (browser == null) {
            browser = Browser.from((String) capabilities.get("browserName"));
        }
        return browser;
    }

    public String getBrowserVersion() {
        if (browserVersion == null) {
            browserVersion = (String) capabilities.get("browserVersion");
        }
        return browserVersion;
    }

    public Map<String, Object> getCapabilities() {
        return unmodifiableMap(capabilities);
    }

    public State getState() {
        return state;
    }

    public Session getSession() {
        return session;
    }

    public Slot withSession(Session session) {
        Slot copy = copy();
        copy.session = session;
        copy.state = session != null ? State.USED : State.FREE;
        return copy;
    }

    public Slot withState(State state) {
        Slot copy = copy();
        copy.state = state;
        return copy;
    }

    protected Slot copy() {
        return (Slot) super.copy();
    }

    public enum State {
        FREE,
        USED
    }

    public static class Builder extends NamedIdentityAware.Builder<String> {

        private final Node node;
        private final Map<String, Object> capabilities = new HashMap<>();
        private Browser browser = Browser.OTHER;
        private String browserVersion = StringUtils.NA_STRING;
        private Session session;

        public Builder(String id, Node node) {
            super(id);
            this.node = node;
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

        public Builder session(Session session) {
            this.session = session;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Slot(node);
        }

        public Slot build() {
            Slot slot = (Slot) super.build();
            slot.capabilities = capabilities;
            slot.session = session;
            slot.state = session != null ? State.USED : State.FREE;
            return slot;
        }
    }
}
