package net.microfalx.zenith.api.common;

import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A log entry related to a Selenium session.
 */
@Getter
@ToString
public class Log implements Serializable {

    @Serial
    private static final long serialVersionUID = 5531307073375732448L;

    private final Type type;
    private final String text;

    public static Log create(Type type) {
        return create(type, "No logs available");
    }

    public static Log create(Type type, String text) {
        return new Log(type, text);
    }

    private Log(Type type, String text) {
        requireNonNull(text);
        requireNonNull(text);
        this.type = type;
        this.text = text;
    }

    public enum Type {
        ALL,
        SELENIUM,
        SELENIUM_CLIENT,
        SELENIUM_DRIVER,
        SELENIUM_SERVER,
        BROWSER
    }
}
