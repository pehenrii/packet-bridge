package com.pehenrii.packet.bridge.service;

import com.github.luben.zstd.Zstd;
import com.pehenrii.packet.bridge.annotation.PacketInfo;
import com.pehenrii.packet.bridge.buffer.PacketByteBufferInput;
import com.pehenrii.packet.bridge.buffer.PacketByteBufferOutput;
import com.pehenrii.packet.bridge.packet.Packet;
import com.pehenrii.packet.bridge.packet.PacketHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Service responsible for managing the sending and receiving of packets
 * through Redis Pub/Sub, including serialization, deserialization,
 * compression, and handler registration.
 */
public class PacketService extends RedisPubSubAdapter<byte[], byte[]> implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(PacketService.class);

    private final String baseChannel;
    private final ThreadPoolExecutor executor;

    private final StatefulRedisConnection<byte[], byte[]> sender;
    private final StatefulRedisPubSubConnection<byte[], byte[]> receiver;
    private final RedisCommands<String, String> syncCommands;

    private final Map<String, PacketHandler<?>> packetHandlers = new ConcurrentHashMap<>();

    public PacketService(@NonNull RedisClient redisClient, @NonNull String baseChannel) {
        this.baseChannel = baseChannel;
        this.executor = new ThreadPoolExecutor(
                1, 2, 30,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("packet-messaging"));

        this.sender = redisClient.connect(new ByteArrayCodec());
        this.receiver = redisClient.connectPubSub(new ByteArrayCodec());
        this.syncCommands = redisClient.connect().sync();

        receiver.addListener(this);
    }

    /**
     * Registers a packet handler for a specific packet class.
     * Subscribes to the corresponding channel for receiving packets.
     *
     * @param packetClazz   the class of the packet
     * @param packetHandler the handler to process received packets
     * @param <T>           the type of packet
     */
    public <T extends Packet> void register(@NonNull Class<T> packetClazz, @NonNull PacketHandler<T> packetHandler) {
        String channel = getPacketChannel(packetClazz);

        packetHandlers.put(channel, packetHandler);
        receiver.async().subscribe(channel.getBytes());
    }

    /**
     * Executes a provided operation using the synchronous Redis commands.
     *
     * @param operation Function that receives {@link RedisCommands} and returns a result of type {@code T}.
     * @param <T>       The type of the result returned by the operation.
     * @return The result of executing the {@code operation} function.
     */
    public <T> T executeOperation(@NonNull Function<RedisCommands<String, String>, T> operation) {
        return operation.apply(syncCommands);
    }

    /**
     * Sends a packet using its associated channel.
     *
     * @param packet the packet to send
     */
    public void sendPacket(@NonNull Packet packet) {
        String channel = getPacketChannel(packet.getClass());
        sendPacket(packet, channel);
    }

    /**
     * Sends a packet to a specific channel.
     * The packet is serialized and compressed before being published.
     *
     * @param packet  the packet to send
     * @param channel the channel to publish the packet to
     */
    public void sendPacket(@NonNull Packet packet, @NonNull String channel) {
        CompletableFuture.runAsync(() -> {
            PacketByteBufferOutput buffer = new PacketByteBufferOutput();
            packet.write(buffer);
            byte[] payload = buffer.toByteArray();

            sender.sync().publish(channel.getBytes(), compress(payload));
        }, executor)
                .orTimeout(5 * 1000, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    logger.error("Failed to send packet to channel {}: {}", channel, throwable.getMessage());
                    return null;
                });
    }

    /**
     * Handles incoming messages from subscribed channels.
     * Deserializes and decompresses the packet, then delegates to the appropriate handler.
     *
     * @param channel the channel from which the message was received
     * @param message the received message payload
     */
    @SuppressWarnings("unchecked")
    @Override
    public void message(byte[] channel, byte[] message) {
        executor.execute(() -> {
            try {
                PacketHandler<?> packetHandler = packetHandlers.get(new String(channel));
                if (packetHandler == null) return;

                Class<?> packetClass = packetHandler.getPacketClass();
                Constructor<?> constructor = packetClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Packet packet = (Packet) constructor.newInstance();

                PacketByteBufferInput buffer = new PacketByteBufferInput(decompress(message));
                packet.read(buffer);

                ((PacketHandler<Packet>) packetHandler).onReceive(packet);

            } catch (InstantiationException
                     | IllegalAccessException
                     | NoSuchMethodException
                     | InvocationTargetException e) {
                logger.error("Failed to process packet from channel {}: {}", new String(channel), e.getMessage());
            }
        });
    }

    /**
     * Shuts down the PacketService, closing Redis connections and the executor.
     */
    public void shutdown() {
        try {
            sender.close();
            receiver.close();
            executor.shutdown();
        } catch (Exception ignored) {
        }
    }

    /**
     * Retrieves the channel name for a given packet class based on its {@link PacketInfo} annotation.
     *
     * @param clazz the packet class
     * @return the channel name
     * @throws IllegalArgumentException if the class does not have a {@link PacketInfo} annotation
     */
    @NonNull
    private String getPacketChannel(@NonNull Class<?> clazz) {
        PacketInfo packetInfo = clazz.getAnnotation(PacketInfo.class);
        if (packetInfo == null) {
            throw new IllegalArgumentException("Class " + clazz + " has no @PacketInfo annotation");
        }

        return packetInfo.channel().isEmpty()
                ? baseChannel + ":" + packetInfo.name()
                : packetInfo.channel() + ":" + packetInfo.name();
    }

    /**
     * Compresses a byte array using Zstandard.
     *
     * @param bytes the data to compress
     * @return the compressed data
     */
    private byte[] compress(byte[] bytes) {
        return Zstd.compress(bytes);
    }

    /**
     * Decompresses a byte array using Zstandard.
     *
     * @param bytes the compressed data
     * @return the decompressed data
     */
    private byte[] decompress(byte[] bytes) {
        return Zstd.decompress(bytes, (int) Zstd.decompressedSize(bytes));
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }
}
