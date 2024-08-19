package net.microfalx.zenith.base.rest;

public class RestException extends RuntimeException {

    public RestException(String message) {
        super(message);
    }

    public RestException(String message, Throwable cause) {
        super(message, cause);
    }
}
