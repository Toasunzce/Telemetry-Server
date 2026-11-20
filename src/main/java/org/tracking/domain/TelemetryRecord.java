package org.tracking.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class TelemetryRecord {
    private final Instant time;
    private final GeoPoint position;
    private final List<Measurement> measurements;

    public TelemetryRecord(Instant time, GeoPoint position, List<Measurement> measurements) {
        this.time = Objects.requireNonNull(time, "time must not be null");
        Objects.requireNonNull(measurements, "measurements must not be null");
        this.position = position;
        if (measurements.isEmpty()) {
            throw new IllegalArgumentException("measurements can't be empty");
        }
        this.measurements = List.copyOf(measurements);
    }

    public Instant getTime() {
        return time;
    }

    public Optional<GeoPoint> getPosition() {
        return Optional.ofNullable(position);
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }
}
