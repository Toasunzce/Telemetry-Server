package org.tracking.infrastructure.teltonika;

import org.tracking.application.port.ProtocolException;
import org.tracking.domain.GeoPoint;
import org.tracking.domain.Measurement;
import org.tracking.domain.TelemetryBatch;
import org.tracking.domain.TelemetryRecord;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public final class AvlPackageDecoder {
    private AvlPackageDecoder() {}

    static TelemetryBatch decode(Codec codec, byte[] records, int count, String deviceId) throws ProtocolException{
        ByteBuffer buf = ByteBuffer.wrap(records);
        List<TelemetryRecord> items = new ArrayList<>(count);
        try {
            for (int i = 0; i < count; i++) {
                items.add(readRecord(codec, buf));
            }
            if (buf.hasRemaining()) {
                throw new ProtocolException("unexpected trailing bytes: " + buf.remaining());
            }
            return new TelemetryBatch(deviceId, items);
        } catch (BufferUnderflowException | IllegalArgumentException e) {
            throw new ProtocolException("malformed AVL data: " + e.getMessage(), e);
        }
    }

    private static TelemetryRecord readRecord(Codec codec, ByteBuffer buf) throws ProtocolException {
        Instant time = Instant.ofEpochMilli(buf.getLong());
        int priority = buf.get() & 0xFF;

        int lon = buf.getInt();
        int lat = buf.getInt();
        int altitude = buf.getShort();
        int angle = buf.getShort() & 0xFFFF;
        int satellites = buf.get() & 0xFF;
        int speed = buf.getShort() & 0xFFFF;

        readUnsigned(buf, codec.ioElementLength);
        if (codec.hasGenerationType) {
            buf.get();
        }
        readUnsigned(buf, codec.countLength);

        List<Measurement> measurements = new ArrayList<>();
        measurements.add(Measurement.decimal("teltonika.priority", priority));

        for (int width : codec.sizeGroups) {
            long n = readUnsigned(buf, codec.countLength);
            for (long i = 0; i < n; i++) {
                int id = (int) readUnsigned(buf, codec.ioElementLength);
                long raw = readUnsigned(buf, width);
                /*
                TODO:
                  here we are storing variables just by their name and values
                  we have to map DEVICE NAME, VALUES and PROPERTY name
                   e.g.:
                 >>> Measurement.decimal("Battery voltage", 600, "mV");
                 >>> Measurement.text("GSM signal", "3");
                */
                measurements.add(width == 8
                        ? Measurement.text(Integer.toString(id), Long.toUnsignedString(raw))
                        : Measurement.decimal(Integer.toString(id), raw, ""));
            }
        }
        if (codec.hasVariableIo) {
            long n = readUnsigned(buf, codec.countLength);
            for (long i = 0; i < n; i++) {
                int id = (int) readUnsigned(buf, codec.ioElementLength);
                long length = readUnsigned(buf, codec.variableLengthWidth);
                if (length > buf.remaining()) {
                    throw new ProtocolException("variable IO is longer than the packet: " + length);
                }
                byte[] value = new byte[(int) length];
                buf.get(value);
                if (value.length > 0) {
                    measurements.add(Measurement.text(Integer.toString(id), toHex(value)));
                }
            }
        }

        GeoPoint position = null;
        if (satellites > 0) {
            position = GeoPoint.builder(lat / 1e7, lon / 1e7)
                    .altitudeMeters(altitude)
                    .headingDegrees(angle)
                    .satellites(satellites)
                    .speedKmh(speed)
                    .build();
        }
        return new TelemetryRecord(time, position, measurements);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static long readUnsigned(ByteBuffer buf, int width) {
        long value = 0;
        for (int i = 0; i < width; i++) {
            value = (value << 8) | (buf.get() & 0xFF);
        }
        return value;
    }
}
