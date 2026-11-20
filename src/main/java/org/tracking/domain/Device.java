package org.tracking.domain;

import java.util.Objects;

public final class Device {
    private final String id;
    private final String protocol;
    private final boolean allowed;

    public Device(String id, String protocol, boolean allowed) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be empty: \"" + id + "\"");
        }
        Objects.requireNonNull(protocol, "protocol must not be null");
        if (protocol.isBlank()) {
            throw new IllegalArgumentException("protocol must not be empty: \"" + protocol + "\"");
        }
        this.id = id.trim();
        this.protocol = protocol.trim();
        this.allowed = allowed;
    }

    public String getId() {
        return id;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isAllowed() {
        return allowed;
    }
}
