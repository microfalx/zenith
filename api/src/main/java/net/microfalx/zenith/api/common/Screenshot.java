package net.microfalx.zenith.api.common;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.Identifiable;

import java.io.Serializable;
import java.util.UUID;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A Selenium screenshot.
 */
@Getter
@ToString
public class Screenshot implements Identifiable<String>, Serializable {

    private static final long serialVersionUID = 1943103950177765888L;

    private static byte[] NO_DATA = new byte[0];

    private final String id;
    private final byte[] data;

    public static Screenshot create() {
        return create(UUID.randomUUID().toString(), NO_DATA);
    }

    public static Screenshot create(String id, byte[] data) {
        return new Screenshot(id, data);
    }

    private Screenshot(String id, byte[] data) {
        requireNonNull(id);
        requireNonNull(data);
        this.id = id;
        this.data = data;
    }

    @Override
    public String getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public boolean exists() {
        return data.length > 0;
    }
}
