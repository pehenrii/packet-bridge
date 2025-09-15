# Packet-Bridge

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![Redis](https://img.shields.io/badge/Redis-6.0+-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-green.svg)]()

**Packet-Bridge**  is a lightweight and efficient Java framework for inter-service communication using Redis Pub/Sub with binary serialization and automatic compression.

## Características

- **Efficient Binary Serialization**: Custom serialization with low overhead
- **Automatic Compression**: Uses Zstandard for better data compression
- **High Performance**: Optimized for low latency and high throughput
- **Channel Based**: Channel separation based on annotations

## Exemplos de Uso

### 1. Creating a Packet

```java
@PacketInfo(name = "user-login", channel = "auth")
public class UserLoginPacket implements Packet {
    private UUID userId;
    private String username;
    private long timestamp;
    private boolean rememberMe;

    // No-arg constructor for deserialization
    public UserLoginPacket() {}

    public UserLoginPacket(UUID userId, String username, boolean rememberMe) {
        this.userId = userId;
        this.username = username;
        this.timestamp = System.currentTimeMillis();
        this.rememberMe = rememberMe;
    }

    @Override
    public void write(PacketByteBufferOutput buffer) {
        buffer.writeUUID(userId)
              .writeString(username)
              .writeLong(timestamp)
              .writeBoolean(rememberMe);
    }

    @Override
    public void read(PacketByteBufferInput buffer) {
        this.userId = buffer.readUUID();
        this.username = buffer.readString();
        this.timestamp = buffer.readLong();
        this.rememberMe = buffer.readBoolean();
    }

    // Getters e setters...
}
```

### 2. Creating a Handler

```java
public class UserLoginHandler implements PacketHandler<UserLoginPacket> {
    
    @Override
    public void onReceive(UserLoginPacket packet) {
        System.out.println("Usuário logou: " + packet.getUsername());
        System.out.println("Timestamp: " + packet.getTimestamp());
        
        // Authentication logic here
        authenticateUser(packet.getUserId(), packet.getUsername());
    }

    @Override
    public Class<UserLoginPacket> getPacketClass() {
        return UserLoginPacket.class;
    }
}
```

### 3. Configuring the Service

```java
public class Application {
    static void main(String[] args) {
        PacketMessage packetMessage = PacketMessage.create(
            "localhost", // Redis host
            6379, // Redis port  
            "password", // Redis password (leave empty if none)
            "app-channel" // Default channel for application
        );

        // Register handler
        packetMessage.registerPacket(UserLoginPacket.class, new UserLoginHandler());

        // Send the packet
        UserLoginPacket loginPacket = new UserLoginPacket(
            UUID.randomUUID(),
            "usuario123", 
            true
        );
        
        packetMessage.sendPacket(loginPacket);
    }
}
```

## Troubleshooting

### Common Issues

**Packet not being received:**
- Verify if the handler was registered before sending
- Confirm if the channel is correct in the `@PacketInfo` annotation
- Check deserialization error logs

**Low performance:**
- Monitor CPU/memory usage
- Check network latency with Redis
- Consider using Redis Cluster for high availability

**Serialization error:**
- Ensure the packet has a default constructor
- Verify if the write/read order is correct
- Test serialization in isolation

## Licença

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.