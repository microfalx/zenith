package net.microfalx.zenith.api.node;


import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.zenith.api.common.Server;

import java.io.Serial;
import java.io.Serializable;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A Selenium node (worker).
 */
@Getter
@ToString
public class Node extends NamedIdentityAware<String> implements Serializable {

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
}
