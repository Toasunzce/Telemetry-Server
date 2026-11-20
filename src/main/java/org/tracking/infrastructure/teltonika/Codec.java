package org.tracking.infrastructure.teltonika;

import org.tracking.application.port.ProtocolException;

public enum Codec {

    CODEC_8 (0x08, 1, 1, false, false),
    CODEC_8E(0x8E, 2, 2, false, true),
    CODEC_16(0x10, 2, 1, true, false);

    final int id;
    final int ioElementLength;
    final int countLength;
    final boolean hasGenerationType;
    final boolean hasVariableIo;
    final int variableLengthWidth = 2;
    final int[] sizeGroups = {1, 2, 4, 8};

    Codec(int id, int ioElementLength, int countLength, boolean hasGenerationType, boolean hasVariableIo) {
        this.id = id;
        this.ioElementLength = ioElementLength;
        this.countLength = countLength;
        this.hasGenerationType = hasGenerationType;
        this.hasVariableIo = hasVariableIo;
    }

    static Codec byId(int id) throws ProtocolException {
        for (Codec codec : values()) {
            if (codec.id == id) {
                return codec;
            }
        }
        throw new ProtocolException("unsupported codec: 0x" + Integer.toHexString(id));
    }
}