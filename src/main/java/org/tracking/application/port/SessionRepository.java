package org.tracking.application.port;

import org.tracking.domain.Session;

public interface SessionRepository {
    boolean save(Session session);
}
