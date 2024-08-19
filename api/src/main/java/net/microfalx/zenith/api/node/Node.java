package net.microfalx.zenith.api.node;


import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.zenith.api.common.Server;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;

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

    private final Server server;
    private final int port;

    public static Node create(Server server, int port) {
        return new Node(server, port);
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
}
