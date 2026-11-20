package org.tracking.application.port;

import org.tracking.domain.TelemetryBatch;

public interface TelemetryRepository {
    boolean save(TelemetryBatch batch);
}
