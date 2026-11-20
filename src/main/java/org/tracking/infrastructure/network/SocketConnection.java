package org.tracking.infrastructure.network;

import org.tracking.application.port.DeviceConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

public class SocketConnection implements DeviceConnection {

    private final InputStream input;
    private final OutputStream output;
    private final String remoteAddress;

    public SocketConnection(Socket socket) throws IOException {
        Objects.requireNonNull(socket, "socket must not be empty");
        this.remoteAddress = socket.getRemoteSocketAddress().toString();
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
    }

    @Override
    public InputStream input() {
        return input;
    }

    @Override
    public OutputStream output() {
        return output;
    }

    @Override
    public String remoteAddress() {
        return remoteAddress;
    }
}
