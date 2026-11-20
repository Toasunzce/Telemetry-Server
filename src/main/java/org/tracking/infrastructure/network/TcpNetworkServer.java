package org.tracking.infrastructure.network;

import org.tracking.application.port.ProtocolException;
import org.tracking.application.services.DeviceSessionService;
import org.tracking.application.port.NetworkServer;
import org.tracking.application.port.ProtocolStrategy;
import org.tracking.domain.Session;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TcpNetworkServer implements NetworkServer {
    private static final Logger LOG = Logger.getLogger(TcpNetworkServer.class.getName());

    private final int port;
    private final DeviceSessionService service;
    private final ProtocolStrategy protocol;
    private final Semaphore connectionLimit;
    private final int timeoutMs;
    private final AtomicBoolean running;

    private volatile ExecutorService executor;
    private volatile ServerSocket serverSocket;

    private enum Reason {
        CLOSED_BY_DEVICE("closed by device"),
        TIMEOUT("timeout"),
        IO_ERROR("io error"),
        PROTOCOL_ERROR("protocol error"),
        INTERNAL_ERROR("internal error");

        private final String text;

        Reason(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public TcpNetworkServer(int port,
                            ProtocolStrategy protocol,
                            DeviceSessionService service,
                            int timeoutMs,
                            int maxConnections) {
        Objects.requireNonNull(protocol, "protocol must not be null");
        Objects.requireNonNull(service, "service must not be null");
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("port out of range: " + port);
        }
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be > 0: " + timeoutMs);
        }
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("maxConnections must be > 0");
        }

        this.running = new AtomicBoolean();
        this.port = port;
        this.protocol = protocol;
        this.service = service;
        this.timeoutMs = timeoutMs;
        this.connectionLimit = new Semaphore(maxConnections);
    }

    @Override
    public void start() throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("server is already running");
        }

        ExecutorService pool = Executors.newCachedThreadPool();
        ServerSocket server;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            running.set(false);
            pool.shutdown();
            throw e;
        }

        serverSocket = server;
        executor = pool;
        LOG.info("listening on port " + port);

        try (server) {
            while (running.get()) {
                Socket socket;
                try {
                    socket = server.accept();
                } catch (SocketException e) {
                    if (!running.get()) {
                        break;
                    }
                    throw e;
                }

                if (!connectionLimit.tryAcquire()) {
                    closeQuietly(socket);
                    continue;
                }

                try {
                    pool.execute(() -> {
                        try {
                            handle(socket);
                        } finally {
                            connectionLimit.release();
                        }
                    });
                } catch (RejectedExecutionException e) {
                    connectionLimit.release();
                    closeQuietly(socket);
                }
            }
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        running.set(false);
        ServerSocket server = serverSocket;
        if (server != null) {
            closeQuietly(server);
        }
        ExecutorService pool = executor;
        if (pool != null) {
            pool.shutdown();
        }
    }

    private static void closeQuietly(java.io.Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignored) {
            // ???
        }
    }

    private void handle(Socket socket)  {
        String reason = null;
        Session session = service.openSession(protocol.name(), String.valueOf(socket.getRemoteSocketAddress()));
        try (socket) {
            socket.setSoTimeout(timeoutMs);
            protocol.serve(new SocketConnection(socket), session, service);
        } catch (SocketTimeoutException e) {
            reason = Reason.TIMEOUT.toString();
        } catch (ProtocolException e) {
            LOG.warning("protocol error from " + session.getConnectionAddress() + ": " + e.getMessage());
            reason = Reason.PROTOCOL_ERROR.toString();
        } catch (IOException e) {
            LOG.warning("io error from " + session.getConnectionAddress() + ": " + e.getMessage());
            reason = Reason.IO_ERROR.toString();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "internal error in session " + session.getSessionId(), e);
            reason = Reason.INTERNAL_ERROR.toString();
        } finally {
            if (reason == null) {
                reason = Reason.CLOSED_BY_DEVICE.toString();
            }
            service.closeSession(session, reason);
        }
    }
}
