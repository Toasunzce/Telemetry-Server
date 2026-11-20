package org.tracking.application.port;

import org.tracking.domain.Device;

import java.util.Optional;

public interface DeviceRepository {
    boolean save(Device device);

    Optional<Device> findById(String deviceId);
}
