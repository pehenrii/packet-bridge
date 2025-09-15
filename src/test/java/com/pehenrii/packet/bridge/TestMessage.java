package com.pehenrii.packet.bridge;

import com.pehenrii.packet.bridge.annotation.PacketInfo;
import com.pehenrii.packet.bridge.buffer.PacketByteBufferInput;
import com.pehenrii.packet.bridge.buffer.PacketByteBufferOutput;
import com.pehenrii.packet.bridge.packet.Packet;

import java.util.UUID;

@PacketInfo(name = "test-message")
public class TestMessage implements Packet {

    private UUID uniqueId;
    private String content;
    private int number;
    private boolean flag;

    public TestMessage() {}

    public TestMessage(UUID uniqueId, String content, int number, boolean flag) {
        this.uniqueId = uniqueId;
        this.content = content;
        this.number = number;
        this.flag = flag;
    }

    @Override
    public void write(PacketByteBufferOutput buffer) {
        buffer.writeUUID(uniqueId)
                .writeString(content)
                .writeInt(number)
                .writeBoolean(flag);
    }

    @Override
    public void read(PacketByteBufferInput buffer) {
        this.uniqueId = buffer.readUUID();
        this.content = buffer.readString();
        this.number = buffer.readInt();
        this.flag = buffer.readBoolean();
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getContent() {
        return content;
    }

    public int getNumber() {
        return number;
    }

    public boolean isFlag() {
        return flag;
    }
}
