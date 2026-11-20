package org.tracking.infrastructure.database;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.tracking.application.port.DeviceRepository;
import org.tracking.domain.Device;

import java.util.Objects;
import java.util.Optional;

public class PostgresDeviceRepository implements DeviceRepository {
    private static final String SQL_QUERY =
            "INSERT INTO device (id, protocol, allowed) VALUES (:id, :protocol, :allowed) "
          + "ON CONFLICT (id) DO UPDATE SET protocol = :protocol, allowed = :allowed";

    private final Jdbi ds;
    private final boolean autoRegister;
    private final String autoProtocol;

    public PostgresDeviceRepository(Jdbi ds, boolean autoRegister, String autoProtocol) {
        this.ds = Objects.requireNonNull(ds, "data source must not be null");
        this.autoRegister = autoRegister;
        this.autoProtocol = Objects.requireNonNull(autoProtocol, "protocol name must not be null");
    }

    @Override
    public boolean save(Device device) {
        try {
            ds.useHandle(h -> h.createUpdate(SQL_QUERY)
                    .bind("id", device.getId())
                    .bind("protocol", device.getProtocol())
                    .bind("allowed", device.isAllowed())
                    .execute());
            return true;
        } catch (JdbiException e) {
            return false;
        }
    }

    @Override
    public Optional<Device> findById(String deviceId) {
        Optional<Device> found;
        try {
            found = ds.withHandle(h -> h
                    .createQuery("SELECT id, protocol, allowed FROM device WHERE id = :id")
                    .bind("id", deviceId)
                    .map((rs, ctx) -> new Device(
                            rs.getString("id"),
                            rs.getString("protocol"),
                            rs.getBoolean("allowed")))
                    .findFirst());
        } catch (JdbiException e) {
            return Optional.empty();
        }

        if (found.isPresent() || !autoRegister) {
            return found;
        }

        Device created = new Device(deviceId, autoProtocol, true);
        if (save(created)) {
            return Optional.of(created);
        } else {
            return Optional.empty();
        }
    }
}