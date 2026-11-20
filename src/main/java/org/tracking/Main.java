package org.tracking;

import org.jdbi.v3.core.Jdbi;
import org.tracking.application.services.DeviceSessionService;
import org.tracking.application.port.*;
import org.tracking.infrastructure.database.*;
import org.tracking.infrastructure.network.TcpNetworkServer;
import org.tracking.infrastructure.teltonika.TeltonikaProtocol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        Map<String, String> options = parseArgs(args);
        int port = Integer.parseInt(options.getOrDefault("--port", "5027"));
        int timeoutMs = Integer.parseInt(options.getOrDefault("--timeout-ms", "600000"));
        int maxConnections = Integer.parseInt(options.getOrDefault("--max-connections", "500"));
        boolean autoRegister = Boolean.parseBoolean(options.getOrDefault("--auto-register", "true"));

        Jdbi ds = Jdbi.create(DataSourceFactory.create());

        DeviceSessionService service = new DeviceSessionService(
                new PostgresDeviceRepository(ds, true, "teltonika"),
                new PostgresTelemetryRepository(ds),
                new PostgresSessionRepository(ds));
        ProtocolStrategy protocol = new TeltonikaProtocol();

        NetworkServer server = new TcpNetworkServer(port, protocol, service, timeoutMs, maxConnections);
        server.start();
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i + 1 < args.length; i += 2) {
            options.put(args[i], args[i+1]);
        }
        return options;
    }
}
