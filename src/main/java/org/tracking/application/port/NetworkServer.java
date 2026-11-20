package org.tracking.application.port;

import java.io.IOException;

public interface NetworkServer {
    public void start() throws IOException;

    public void stop(); // session loop
}
