package net.microfalx.zenith.api.node;

/**
 * Base class for all node exceptions.
 */
public class NodeException extends RuntimeException {

    public NodeException(String message) {
        super(message);
    }

    public NodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
