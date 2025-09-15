package com.pehenrii.packet.bridge.buffer;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class PacketByteBufferOutput {

    private final ByteArrayDataOutput buffer;

    public PacketByteBufferOutput() {
        this.buffer = ByteStreams.newDataOutput();
    }

    public PacketByteBufferOutput writeByte(byte value) {
        buffer.writeByte(value);
        return this;
    }

    public PacketByteBufferOutput writeBoolean(boolean value) {
        buffer.writeBoolean(value);
        return this;
    }

    public PacketByteBufferOutput writeShort(short value) {
        buffer.writeShort(value);
        return this;
    }

    public PacketByteBufferOutput writeInt(int value) {
        buffer.writeInt(value);
        return this;
    }

    public PacketByteBufferOutput writeLong(long value) {
        buffer.writeLong(value);
        return this;
    }

    public PacketByteBufferOutput writeFloat(float value) {
        buffer.writeFloat(value);
        return this;
    }

    public PacketByteBufferOutput writeDouble(double value) {
        buffer.writeDouble(value);
        return this;
    }

    public PacketByteBufferOutput writeString(String value) {
        if (value == null) {
            writeBoolean(false);
            return this;
        }

        writeBoolean(true);
        buffer.writeUTF(value);
        return this;
    }

    public PacketByteBufferOutput writeOptionalString(@Nullable String value) {
        writeBoolean(value != null);
        if (value != null) {
            writeString(value);
        }

        return this;
    }

    public PacketByteBufferOutput writeUUID(UUID value) {
        buffer.writeLong(value.getMostSignificantBits());
        buffer.writeLong(value.getLeastSignificantBits());
        return this;
    }

    public PacketByteBufferOutput writeOptionalUUID(@Nullable UUID value) {
        writeBoolean(value != null);
        if (value != null) {
            writeUUID(value);
        }

        return this;
    }

    public <T extends Enum<T>> PacketByteBufferOutput writeEnum(Enum<T> value) {
        buffer.writeUTF(value.name());
        return this;
    }

    public <T> PacketByteBufferOutput writeOptional(@Nullable T value, Consumer<T> writer) {
        writeBoolean(value != null);
        if (value != null) {
            writer.accept(value);
        }

        return this;
    }

    public byte[] toByteArray() {
        return buffer.toByteArray();
    }
}
