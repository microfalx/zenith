package net.microfalx.zenith.api.hub;


import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.zenith.api.common.Server;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A central hub for all Selenium nodes.
 */
@Getter
@ToString
public class Hub extends NamedIdentityAware<String> implements Serializable {

    public static final String HUB_PATH = "/wd/hub";
    public static final String STATUS_PATH = "/status";

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

    /**
     * Returns the URI to the Selenium HUB web service.
     *
     * @return a non-null instance
     */
    public URI getWsUri() {
        return URI.create(getUri().toASCIIString() + HUB_PATH);
    }

}
