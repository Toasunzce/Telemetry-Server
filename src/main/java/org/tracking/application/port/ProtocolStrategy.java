package org.tracking.application.port;

import org.tracking.application.services.DeviceSessionService;
import org.tracking.domain.Session;

import java.io.IOException;

public interface ProtocolStrategy {
    String name();

    // handshake, etc
    void serve(DeviceConnection connection, Session session, DeviceSessionService service) throws IOException;
}
