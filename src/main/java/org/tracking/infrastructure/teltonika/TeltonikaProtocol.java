package org.tracking.infrastructure.teltonika;

import org.tracking.application.services.DeviceSessionService;
import org.tracking.application.port.DeviceConnection;
import org.tracking.application.port.ProtocolStrategy;
import org.tracking.domain.Session;
import org.tracking.domain.TelemetryBatch;
import org.tracking.application.port.ProtocolException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class TeltonikaProtocol implements ProtocolStrategy {

    private static final int MAX_DATA_LENGTH = 2048;
    private static final int MAX_IMEI_LENGTH = 128;

    @Override
    public String name() {
        return "teltonika";
    }

    @Override
    public void serve(DeviceConnection connection, Session session, DeviceSessionService service) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(connection.input()));
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(connection.output()));

        // pre-AVL looping
        String imei = readImei(in);
        boolean result = service.identifyDevice(session, imei);
        out.writeByte(result ? 0x01 : 0x00);
        out.flush();
        if (!result) {
            return;
        }

        // AVL looping
        byte[] data;
        while ((data = readPacket(in)) != null) {
            Codec codec = Codec.byId(data[0] & 0xFF);
            int count = data[1] & 0xFF;
            byte[] records = Arrays.copyOfRange(data, 2, data.length - 1);
            TelemetryBatch batch = AvlPackageDecoder.decode(codec, records, count, imei);
            if (service.acceptBatch(session, batch)) {
                out.writeInt(count);
                out.flush();
            }
        }
    }

    private static String readImei(DataInputStream in) throws IOException {
        int length = in.readUnsignedShort();
        if (length < 1 || length > MAX_IMEI_LENGTH) {
            throw new ProtocolException("bad IMEI length: " + length);
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private static byte[] readPacket(DataInputStream in) throws IOException {
        int preamble;
        try {
            preamble = in.readInt();
        } catch (EOFException e) {
            return null;
        }

        if (preamble != 0x00000000) {
            throw new ProtocolException("bad preamble: " + preamble);
        }

        int length = in.readInt();
        if (length < 3 || length > MAX_DATA_LENGTH) {
            throw new ProtocolException("bad data length: " + length);
        }

        byte[] data = new byte[length];
        in.readFully(data);

        if (in.readInt() != CRC16.process(data)) {
            throw new ProtocolException("CRC mismatch");
        }
        if (data[1] != data[length - 1]) {
            throw new ProtocolException("record counts different");
        }
        return data;
    }
}
