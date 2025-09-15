package com.pehenrii.packet.bridge.buffer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class PacketByteBufferInput {

    private final ByteArrayDataInput buffer;

    public PacketByteBufferInput(byte[] bytes) {
        this.buffer = ByteStreams.newDataInput(bytes);
    }

    public byte readByte() {
        return buffer.readByte();
    }

    public boolean readBoolean() {
        return buffer.readBoolean();
    }

    public short readShort() {
        return buffer.readShort();
    }

    public int readInt() {
        return buffer.readInt();
    }

    public long readLong() {
        return buffer.readLong();
    }

    public float readFloat() {
        return buffer.readFloat();
    }

    public double readDouble() {
        return buffer.readDouble();
    }

    public String readString() {
        try {
            boolean isValid = buffer.readBoolean();
            if (!isValid) return null;

            return buffer.readUTF();
        } catch (Exception e) {
            return null;
        }
    }

    public String readOptionalString() {
        return readBoolean() ? readString() : null;
    }

    public UUID readUUID() {
        return new UUID(buffer.readLong(), buffer.readLong());
    }

    public UUID readOptionalUUID() {
        return readBoolean() ? readUUID() : null;
    }

    public <T extends Enum<T>> T readEnum(Class<T> clazz) {
        try {
            return Enum.valueOf(clazz, buffer.readUTF());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public <T> Optional<T> readOptional(Function<PacketByteBufferInput, T> reader) {
        return readBoolean() ? Optional.of(reader.apply(this)) : Optional.empty();
    }
}
