package org.tracking.infrastructure.database;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.tracking.application.port.TelemetryRepository;
import org.tracking.domain.GeoPoint;
import org.tracking.domain.Measurement;
import org.tracking.domain.TelemetryBatch;
import org.tracking.domain.TelemetryRecord;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PostgresTelemetryRepository implements TelemetryRepository {
    private static final Logger LOG = Logger.getLogger(PostgresTelemetryRepository.class.getName());
    private final Jdbi ds;

    private static final String INSERT_SQL =
            "INSERT INTO telemetry (device_id, time, latitude, longitude, measurements) "
                    + "VALUES (:device, :time, :lat, :lon, CAST(:measurements AS jsonb)) "
                    + "ON CONFLICT (device_id, time) DO NOTHING";

    public PostgresTelemetryRepository(Jdbi ds) {
        this.ds = Objects.requireNonNull(ds);
    }

    @Override
    public boolean save(TelemetryBatch batch) {
        try {
            ds.useTransaction(h -> {
                PreparedBatch pb = h.prepareBatch(INSERT_SQL);
                for (TelemetryRecord r : batch.getRecords()) {
                    Double lat = null;
                    Double lon = null;
                    if (r.getPosition().isPresent()) {
                        GeoPoint p = r.getPosition().get();
                        lat = p.getLatitude();
                        lon = p.getLongitude();
                    }
                    pb.bind("device", batch.getDeviceId())
                            .bind("time", OffsetDateTime.ofInstant(r.getTime(), ZoneOffset.UTC))
                            .bind("lat", lat)
                            .bind("lon", lon)
                            .bind("measurements", toJson(r.getMeasurements()))
                            .add();
                }
                pb.execute();
            });
            return true;
        } catch (JdbiException e) {
        LOG.log(Level.SEVERE, "cannot save telemetry of " + batch.getDeviceId(), e);
        return false;
    }
    }

    private static String toJson(List<Measurement> measurements) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        for (Measurement m : measurements) {
            ObjectNode node = root.putObject(m.getName());
            if (m.isDecimal()) {
                node.put("value", m.getDecimal());
                if (!m.getUnit().isEmpty()) {
                    node.put("unit", m.getUnit());
                }
            } else {
                node.put("value", m.getText());
            }
        }
        return root.toString();
    }
}
