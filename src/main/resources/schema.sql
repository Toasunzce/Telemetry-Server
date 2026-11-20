CREATE TABLE IF NOT EXISTS device (
    id       TEXT,
    protocol TEXT    NOT NULL,
    allowed  BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS session (
    session_id        TEXT,
    device_id         TEXT,       NOT NULL,
    protocol          TEXT        NOT NULL,
    remote_address    TEXT        NOT NULL,
    opened_at         TIMESTAMPTZ NOT NULL,
    closed_at         TIMESTAMPTZ,
    close_reason      TEXT,
    stored_packages   INTEGER     NOT NULL DEFAULT 0,
    stored_records    INTEGER     NOT NULL DEFAULT 0,
    rejected_packages INTEGER     NOT NULL DEFAULT 0,
    PRIMARY KEY (session_id)
    FOREIGN KEY (device_id) REFERENCES device (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS session_device_idx ON session (device_id, opened_at DESC);


CREATE TABLE IF NOT EXISTS telemetry (
    device_id    TEXT             NOT NULL,
    time         TIMESTAMPTZ      NOT NULL,
    latitude     DOUBLE PRECISION,
    longitude    DOUBLE PRECISION,
    measurements JSONB            NOT NULL,
    PRIMARY KEY (device_id, time),
    FOREIGN KEY (device_id) REFERENCES device (id) ON DELETE CASCADE
);

