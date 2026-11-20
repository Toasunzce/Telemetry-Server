package org.tracking.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Session {
    public enum SessionState { OPENED, CLOSED }

    private final String sessionId;
    private final String protocolName;
    private final String connectionAddress;
    private final Instant sessionOpenTime;

    private String deviceId;
    private SessionState state;
    private Instant sessionCloseTime;
    private String closeReason;

    private int storedPackages;
    private int storedRecords;
    private int rejectedPackages;

    private Session(String sessionId, String protocolName, String connectionAddress, Instant openTime) {
        this.sessionId = sessionId;
        this.protocolName = protocolName;
        this.connectionAddress = connectionAddress;
        this.sessionOpenTime = openTime;
        this.state = SessionState.OPENED;
    }

    public static Session open(String protocolName, String connectionAddress, Instant openTime) {
        return new Session(
                UUID.randomUUID().toString(),
                requireNotBlank(protocolName, "protocolName"),
                requireNotBlank(connectionAddress, "connectionAddress"),
                Objects.requireNonNull(openTime, "openTime must not be null")
        );
    }

    public void close(String reason, Instant closeTime) {
        if (state != SessionState.OPENED) {
            throw new IllegalStateException("session " + sessionId + "must be opened");
        }
        Objects.requireNonNull(closeTime, "closeTime must not be null");
        if (closeTime.isBefore(sessionOpenTime)) {
            throw new IllegalArgumentException("closeTime must not be more previous than openTime");
        }
        this.closeReason = requireNotBlank(reason, "reason");
        this.sessionCloseTime = closeTime;
        this.state = SessionState.CLOSED;
    }

    public void identifyDevice(String deviceId) {
        if (state != SessionState.OPENED) {
            throw new IllegalStateException("session " + sessionId + "must be opened");
        }
        this.deviceId = requireNotBlank(deviceId, "deviceId");
    }

    public void registerStored(int records) {
        storedPackages++;
        storedRecords += records;
    }

    public void registerRejected() {
        rejectedPackages++;
    }

    public int getStoredPackages() { return storedPackages; }
    public int getStoredRecords() { return storedRecords; }
    public int getRejectedPackages() { return rejectedPackages; }

    public String getSessionId() {
        return sessionId;
    }
    public String getProtocolName() {
        return protocolName;
    }
    public String getConnectionAddress() {
        return connectionAddress;
    }
    public Instant getSessionOpenTime() {
        return sessionOpenTime;
    }

    public Optional<String> getDeviceId() {
        return Optional.ofNullable(deviceId);
    }
    public Optional<Instant> getSessionCloseTime() {
        return Optional.ofNullable(sessionCloseTime);
    }
    public Optional<String> getCloseReason() {
        return Optional.ofNullable(closeReason);
    }

    private static String requireNotBlank(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return trimmed;
    }

    public boolean isOpen() {
        return state == SessionState.OPENED;
    }
}
