package org.tracking.infrastructure.database;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.tracking.application.port.SessionRepository;
import org.tracking.domain.Session;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgresSessionRepository implements SessionRepository {
    private static final String INSERT_SQL =
            "INSERT INTO session (session_id, device_id, protocol, remote_address, opened_at, closed_at, "
                    + "close_reason, stored_packages, stored_records, rejected_packages) "
                    + "VALUES (:id, :device, :protocol, :address, :opened, :closed, :reason, "
                    + ":stored, :records, :rejected)";

    private static final Logger LOG = Logger.getLogger(PostgresSessionRepository.class.getName());

    private final Jdbi ds;

    public PostgresSessionRepository(Jdbi ds) {
        this.ds = Objects.requireNonNull(ds);
    }

    @Override
    public boolean save(Session s) {
        OffsetDateTime closedAt = s.getSessionCloseTime()
                .map(t -> OffsetDateTime.ofInstant(t, ZoneOffset.UTC))
                .orElse(null);
        try {
            ds.useHandle(h -> h.createUpdate(INSERT_SQL)
                    .bind("id", s.getSessionId())
                    .bind("device", s.getDeviceId().orElse(null))
                    .bind("protocol", s.getProtocolName())
                    .bind("address", s.getConnectionAddress())
                    .bind("opened", OffsetDateTime.ofInstant(s.getSessionOpenTime(), ZoneOffset.UTC))
                    .bindByType("closed", closedAt, OffsetDateTime.class)
                    .bind("reason", s.getCloseReason().orElse(null))
                    .bind("stored", s.getStoredPackages())
                    .bind("records", s.getStoredRecords())
                    .bind("rejected", s.getRejectedPackages())
                    .execute());
            return true;
        } catch (JdbiException e) {
            LOG.log(Level.SEVERE, "cannot save session " + s.getSessionId(), e);
            return false;
        }
    }
}