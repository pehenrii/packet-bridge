package com.pehenrii.packet.bridge.packet;

import com.pehenrii.packet.bridge.buffer.PacketByteBufferInput;
import com.pehenrii.packet.bridge.buffer.PacketByteBufferOutput;

/**
 * Represents a generic packet that can be serialized and deserialized
 * using {@link PacketByteBufferOutput} and {@link PacketByteBufferInput}.
 * <p>
 * Implementations of this interface define how to write their data to an output buffer
 * and how to read their data from an input buffer.
 * </p>
 */
public interface Packet {

    /**
     * Serializes the packet data into the provided output buffer.
     *
     * @param buffer the output buffer to write data to
     */
    void write(PacketByteBufferOutput buffer);

    /**
     * Deserializes the packet data from the provided input buffer.
     *
     * @param buffer the input buffer to read data from
     */
    void read(PacketByteBufferInput buffer);
}
