package net.microfalx.zenith.api.hub;


import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.zenith.api.common.Server;

import java.io.Serial;
import java.io.Serializable;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A central hub for all Selenium nodes.
 */
@Getter
@ToString
public class Hub extends NamedIdentityAware<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = -7895571769149200427L;

    private final Server server;
    private final int port;

    public static Hub create(Server server, int port) {
        return new Hub(server, port);
    }

    private Hub(Server server, int port) {
        requireNonNull(server);
        this.server = server;
        this.setId(server.getId());
        this.setName(server.getName());
        this.port = port;
    }

}
