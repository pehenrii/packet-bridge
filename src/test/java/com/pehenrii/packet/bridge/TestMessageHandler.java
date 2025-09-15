package com.pehenrii.packet.bridge;

import com.pehenrii.packet.bridge.packet.PacketHandler;

import java.util.function.Consumer;

public class TestMessageHandler implements PacketHandler<TestMessage> {

    private final Consumer<TestMessage> onReceiveCallback;

    public TestMessageHandler(Consumer<TestMessage> onReceiveCallback) {
        this.onReceiveCallback = onReceiveCallback;
    }

    @Override
    public void onReceive(TestMessage packet) {
        onReceiveCallback.accept(packet);
    }

    @Override
    public Class<TestMessage> getPacketClass() {
        return TestMessage.class;
    }
}
