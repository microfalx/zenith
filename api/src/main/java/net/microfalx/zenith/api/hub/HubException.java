package net.microfalx.zenith.api.hub;

/**
 * Base exception for all Hub errors.
 */
public class HubException extends RuntimeException {

    public HubException(String message) {
        super(message);
    }

    public HubException(String message, Throwable cause) {
        super(message, cause);
    }
}
