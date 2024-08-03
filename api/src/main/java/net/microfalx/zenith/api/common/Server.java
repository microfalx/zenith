package net.microfalx.zenith.api.common;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.lang.StringUtils;

import java.io.Serializable;

/**
 * A server which identifies a Selenium logical node.
 */
@Getter
@ToString
public class Server extends NamedIdentityAware<String> implements Serializable {

    private String hostname;

    public static Server LOCAL = (Server) new Builder("localhost").name("Local").id("local").build();

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
        public NamedIdentityAware<String> build() {
            Server server = (Server) super.build();
            server.hostname = hostname;
            return server;
        }
    }


}
