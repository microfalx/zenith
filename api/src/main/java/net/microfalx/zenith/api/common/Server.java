package net.microfalx.zenith.api.common;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.lang.StringUtils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A server which identifies a Selenium logical node.
 */
@Getter
@ToString
public class Server extends NamedIdentityAware<String> implements Serializable {

    private String hostname;

    public static Server LOCAL = (Server) new Builder("localhost").name("Local").id("local").build();

    private static volatile Server INSTANCE;

    /**
     * Returns the server where the current process runs.
     *
     * @return a non-null instance
     */
    public static Server get() {
        if (INSTANCE == null) {
            try {
                String hostName = InetAddress.getLocalHost().getHostName();
                INSTANCE = (Server) new Builder(hostName).name(StringUtils.capitalizeWords(hostName)).build();
            } catch (UnknownHostException e) {
                return LOCAL;
            }
        }
        return INSTANCE;
    }

    /**
     * Returns a server by its hostname.
     *
     * @param hostname the hostname
     * @return a non-null instance
     */
    public static Server get(String hostname) {
        return new Server.Builder(hostname).build();
    }

    /**
     * Returns whether the server points to the local loopback interface.
     *
     * @return {@code true} if loopback, {@code false} otherwise
     */
    public boolean isLocal() {
        return getHostname().equals("localhost");
    }

    public static class Builder extends NamedIdentityAware.Builder<String> {

        private String hostname;

        public Builder(String hostname) {
            super(StringUtils.toIdentifier(hostname));
            this.hostname = hostname;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Server();
        }

        @Override
        public Server build() {
            Server server = (Server) super.build();
            server.hostname = hostname;
            return server;
        }
    }


}
