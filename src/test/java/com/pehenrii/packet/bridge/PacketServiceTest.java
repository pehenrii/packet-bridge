package com.pehenrii.packet.bridge;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class PacketServiceTest {

    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "testpass");

    private PacketBridge senderService;
    private PacketBridge receiverService;

    @BeforeEach
    void setup() {
        if (!redis.isRunning()) {
            redis.start();
        }

        String host = redis.getHost();
        Integer port = redis.getMappedPort(6379);
        String password = "testpass";
        String channel = "test-channel";

        senderService = PacketBridge.create(host, port, password, channel);
        receiverService = PacketBridge.create(host, port, password, channel);
    }

    @AfterEach
    void teardown() {
        if (senderService != null) senderService.shutdown();
        if (receiverService != null) receiverService.shutdown();
    }

    @Test
    @DisplayName("Test Packet Sending and Receiving")
    void shouldSendAndReceivePacket() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<TestMessage> receivedPacket = new AtomicReference<>();

        TestMessageHandler handler = new TestMessageHandler(packet -> {
            receivedPacket.set(packet);
            latch.countDown();
        });

        receiverService.registerPacket(TestMessage.class, handler);

        Thread.sleep(100);

        UUID testId = UUID.randomUUID();
        String testContent = "Hello, Packet!";
        TestMessage testMessagePacket = new TestMessage(
                testId,
                testContent,
                42,
                true
        );

        senderService.sendPacket(testMessagePacket);

        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assert messageReceived : "Packet was not received in time";
        assert receivedPacket.get() != null : "Received packet is null";

        TestMessage received = receivedPacket.get();
        assert received.getUniqueId().equals(testMessagePacket.getUniqueId()) : "Unique IDs do not match";
        assert received.getContent().equals(testMessagePacket.getContent()) : "Contents do not match";
        assert received.getNumber() == testMessagePacket.getNumber() : "Numbers do not match";
        assert received.isFlag() == testMessagePacket.isFlag() : "Flags do not match";
    }
}
