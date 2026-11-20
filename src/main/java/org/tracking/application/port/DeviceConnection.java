package org.tracking.application.port;

import java.io.InputStream;
import java.io.OutputStream;

public interface DeviceConnection {
    public InputStream input();

    public OutputStream output();

    public String remoteAddress();
}
