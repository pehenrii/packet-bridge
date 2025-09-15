package com.pehenrii.packet.bridge;

import com.pehenrii.packet.bridge.packet.Packet;
import com.pehenrii.packet.bridge.packet.PacketHandler;
import com.pehenrii.packet.bridge.provider.PacketBridgeProvider;
import io.lettuce.core.api.sync.RedisCommands;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface PacketBridge {

    <T extends Packet> void registerPacket(Class<T> packetClass, PacketHandler<T> handler);

    <T> T executeOperation(Function<RedisCommands<String, String>, T> operation);

    void sendPacket(Packet packet);

    void sendPacket(Packet packet, String channel);

    void shutdown();

    @Contract("_, _, _, _ -> new")
    static @NotNull PacketBridge create(String address, int port, String password, String channel) {
        return new PacketBridgeProvider(address, port, password, channel);
    }
}
