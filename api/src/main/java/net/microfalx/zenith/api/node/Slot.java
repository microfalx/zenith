package net.microfalx.zenith.api.node;

import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedIdentityAware;
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

    private Status status;
    private Session session;

    private Slot(Node node) {
        requireNonNull(node);
        this.setName("Slot ");
        this.setDescription("Node " + node.getName() + ", " + getName());
        this.node = node;
    }

    public Browser getBrowser() {
        if (browser == null) {
            browser = EnumUtils.fromName(Browser.class, (String) capabilities.get("browserName"), Browser.OTHER);
        }
        return browser;
    }

    public Map<String, Object> getCapabilities() {
        return unmodifiableMap(capabilities);
    }

    public Slot withSession(Session session) {
        Slot copy = copy();
        copy.session = session;
        return copy;
    }

    public Slot withStatus(Status status) {
        Slot copy = copy();
        copy.status = status;
        return copy;
    }

    protected Slot copy() {
        return (Slot) super.copy();
    }

    public enum Status {
        FREE,
        USED
    }

    public static class Builder extends NamedIdentityAware.Builder<String> {

        private final Node node;

        private final Map<String, Object> capabilities = new HashMap<>();

        public Builder(String id, Node node) {
            super(id);
            this.node = node;
        }

        public Builder capability(String name, Object value) {
            requireNonNull(name);
            capabilities.put(name, value);
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Slot(node);
        }

        public Slot build() {
            Slot slot = (Slot) super.build();
            slot.capabilities = capabilities;
            return slot;
        }
    }
}
