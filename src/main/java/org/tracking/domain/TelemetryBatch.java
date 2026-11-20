package org.tracking.domain;

import java.util.List;
import java.util.Objects;

public final class TelemetryBatch {
    private final String deviceId;
    private final List<TelemetryRecord> records;

    public TelemetryBatch(String deviceId, List<TelemetryRecord> records) {
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        if (deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId must not be empty: \"" + deviceId + "\"");
        }
        Objects.requireNonNull(records, "records must not be null");

        this.deviceId = deviceId.trim();
        this.records = List.copyOf(records);

        if (this.records.isEmpty()) {
            throw new IllegalArgumentException("records must not be empty");
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public List<TelemetryRecord> getRecords() {
        return records;
    }
}
