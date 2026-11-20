package org.tracking.application.services;

import org.tracking.application.port.DeviceRepository;
import org.tracking.application.port.SessionRepository;
import org.tracking.application.port.TelemetryRepository;
import org.tracking.domain.Device;
import org.tracking.domain.Session;
import org.tracking.domain.TelemetryBatch;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceSessionService {
    private static final Logger LOG = Logger.getLogger(DeviceSessionService.class.getName());

    private final DeviceRepository devices;
    private final TelemetryRepository telemetry;
    private final SessionRepository sessions;

    public DeviceSessionService(DeviceRepository devices, TelemetryRepository telemetry, SessionRepository sessions) {
        this.devices = Objects.requireNonNull(devices, "devices must not be null");
        this.telemetry = Objects.requireNonNull(telemetry, "telemetry must not be null");
        this.sessions = Objects.requireNonNull(sessions, "sessions must not be null");
    }

    public Session openSession(String protocolName, String remoteAddress) {
        return Session.open(protocolName, remoteAddress, Instant.now());
    }

    public boolean identifyDevice(Session session, String deviceId) {
        Optional<Device> device = devices.findById(deviceId);

        if (device.isEmpty()) {
            LOG.warning("unknown device " + deviceId + " from " + session.getConnectionAddress());
            closeSession(session, "unknown device");
            return false;
        }
        if (!device.get().isAllowed()) {
            LOG.warning("device not allowed: " + deviceId);
            closeSession(session, "device not allowed");
            return false;
        }

        session.identifyDevice(device.get().getId());
        LOG.info("device identified: " + deviceId + " from " + session.getConnectionAddress());
        return true;
    }

    public boolean acceptBatch(Session session, TelemetryBatch batch) {
        boolean sameDevice = session.getDeviceId()
                .map(id -> id.equals(batch.getDeviceId()))
                .orElse(false);
        if (!sameDevice) {
            LOG.warning("batch rejected: device mismatch in session " + session.getSessionId());
            session.registerRejected();
            return false;
        }

        boolean stored;
        try {
            stored = telemetry.save(batch);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "failed to store batch of device " + batch.getDeviceId(), e);
            stored = false;
        }

        if (stored) {
            session.registerStored(batch.getRecords().size());
        } else {
            session.registerRejected();
        }
        return stored;
    }

    public void closeSession(Session session, String reason) {
        if (!session.isOpen()) {
            return;
        }
        session.close(reason, Instant.now());
        try {
            if (!sessions.save(session)) {
                LOG.warning("session " + session.getSessionId() + " was not saved");
            }
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "failed to save session " + session.getSessionId(), e);
        }
        LOG.info("session closed (" + reason + "): device=" + session.getDeviceId().orElse("-")
                + " stored=" + session.getStoredRecords() + " rejected=" + session.getRejectedPackages());
    }
}