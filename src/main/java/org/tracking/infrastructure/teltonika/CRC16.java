package org.tracking.infrastructure.teltonika;

public class CRC16 {
    public static int process(byte[] data) {
        int crc = 0;
        for (byte b : data) {
            crc = crc ^ (b & 0xFF);
            for (int i = 0; i < 8; i++) {
                int carry = crc & 1;
                crc = crc >>> 1;
                if (carry == 1) {
                    crc = crc ^ 0xA001;
                }
            }
        }
        return crc & 0xFFFF;
    }
}