# Telemetry-Server

A TCP server that receives telemetry from tracker devices.
Current realization work with Teltonika protocols with imei verifying and AVL packages processing.

## Architecture

The project follows next architecture:

```
org.tracking
├── Main.java
├── domain/                             domain models
├── application/
│   ├── port/                           interfaces
│   └── services/DeviceSessionService   session management
└── infrastructure/
    ├── network/                        TcpNetworkServer, SocketConnection
    ├── teltonika/                      TeltonikaProtocol, AvlPackageDecoder, Codec, CRC16
    └── database/                       postgres repositories
```

## Quick start

### 1. Start the database

```bash
docker compose up -d
```

### 2. Create the tables

You should use `resources/schema.sql` to build necessary tables for this application.

### 3. Run the server

```bash
mvn compile exec:java -Dexec.mainClass=org.tracking.Main
```

With options:

```bash
mvn compile exec:java -Dexec.mainClass=org.tracking.Main \
  -Dexec.args="--port 5027 --timeout-ms 600000 --max-connections 500"
```

The log will show `listening on port 5027`.

## Configuration

### Command-line options

| Option              | Default  | Description                              |
|---------------------|----------|------------------------------------------|
| `--port`            | `5027`   | Server TCP port                          |
| `--timeout-ms`      | `600000` | Idle connection timeout, ms              |
| `--max-connections` | `500`    | Maximum concurrent connections           |
