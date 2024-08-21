package net.microfalx.zenith.api.node;


import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.zenith.api.common.Server;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireBounded;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A Selenium node (worker).
 */
@Getter
@ToString
public class Node extends NamedIdentityAware<String> implements Serializable {

    public static final String STATUS_PATH = "/status";

    @Serial
    private static final long serialVersionUID = -8285666211664115971L;

    public static Node NA = new Node(Server.LOCAL, -1);

    private Server server;
    private int port;
    private int maxSessions = 16;
    private State state = State.INIT;

    public static Builder builder(URI uri) {
        requireNonNull(uri);
        Server server = Server.get(uri.getHost());
        return new Builder(server, uri.getPort());
    }

    public static Builder builder(Server server, int port) {
        return new Builder(server, port);
    }

    public static Node create(Server server, int port) {
        return builder(server, port).build();
    }

    private Node() {
    }

    private Node(Server server, int port) {
        requireNonNull(server);
        this.server = server;
        this.setId(server.getId());
        this.setName(server.getName());
        this.port = port;
    }

    public Server getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public int getMaxSessions() {
        return maxSessions;
    }

    public State getState() {
        return state;
    }

    /**
     * Returns the URI to the Selenium HUB.
     *
     * @return a non-null instance
     */
    public URI getUri() {
        return URI.create("http://" + Server.get().getHostname() + ":" + port);
    }

    /**
     * Returns the URI which can give the status of the HUB.
     *
     * @return a non-null instance
     */
    public URI getStatusUri() {
        return URI.create(getUri().toASCIIString() + STATUS_PATH);
    }

    public enum State {
        INIT,
        UP,
        DOWN
    }

    public static class Builder extends NamedIdentityAware.Builder<String> {

        private final Server server;
        private final int port;
        private int maxSessions = 16;
        private State state = State.INIT;

        Builder(Server server, int port) {
            requireNonNull(server);
            requireBounded(port, 1, 65535);
            this.server = server;
            this.port = port;
        }

        public Builder maxSessions(int maxSessions) {
            this.maxSessions = maxSessions;
            return this;
        }

        public Builder state(State state) {
            requireNonNull(state);
            this.state = state;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Node();
        }

        @Override
        protected String updateId() {
            return server.getId();
        }

        @Override
        public Node build() {
            Node node = (Node) super.build();
            node.setName(server.getName());
            node.server = server;
            node.port = port;
            node.maxSessions = maxSessions;
            node.state = state;
            return node;
        }
    }
}
