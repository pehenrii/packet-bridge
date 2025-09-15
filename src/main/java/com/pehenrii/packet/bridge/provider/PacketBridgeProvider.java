package com.pehenrii.packet.bridge.provider;

import com.pehenrii.packet.bridge.PacketBridge;
import com.pehenrii.packet.bridge.packet.Packet;
import com.pehenrii.packet.bridge.packet.PacketHandler;
import com.pehenrii.packet.bridge.service.PacketService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.function.Function;

public class PacketBridgeProvider implements PacketBridge {

    private final RedisClient redisClient;
    private final PacketService packetService;

    public PacketBridgeProvider(@NonNull String address, int port, @NonNull String password, @NonNull String channel) {
        this.redisClient = RedisClient.create(RedisURI.builder()
                .withHost(address)
                .withPort(port)
                .withPassword(password.toCharArray())
                .withDatabase(0)
                .withTimeout(Duration.ofSeconds(30000))
                .build());

        this.packetService = new PacketService(redisClient, channel);
    }

    @Override
    public <T extends Packet> void registerPacket(Class<T> packetClass, PacketHandler<T> handler) {
        packetService.register(packetClass, handler);
    }

    @Override
    public <T> T executeOperation(Function<RedisCommands<String, String>, T> operation) {
        return packetService.executeOperation(operation);
    }

    @Override
    public void sendPacket(Packet packet) {
        packetService.sendPacket(packet);
    }

    @Override
    public void sendPacket(Packet packet, String channel) {
        packetService.sendPacket(packet, channel);
    }

    @Override
    public void shutdown() {
        packetService.shutdown();
        redisClient.shutdown();
    }
}
