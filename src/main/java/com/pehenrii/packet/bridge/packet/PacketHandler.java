package com.pehenrii.packet.bridge.packet;

/**
 * Handles received packets of a specific type.
 *
 * <p>
 * Implementations of this interface define how to process packets
 * of type {@code T} when they are received.
 * </p>
 *
 * @param <T> the type of packet to handle
 */
public interface PacketHandler<T extends Packet> {

    /**
     * Called when a packet of type {@code T} is received.
     *
     * @param packet the received packet
     */
    void onReceive(T packet);

    /**
     * Returns the class of the packet handled by this handler.
     *
     * @return the packet class
     */
    Class<T> getPacketClass();
}
