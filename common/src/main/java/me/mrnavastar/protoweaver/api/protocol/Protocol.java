package me.mrnavastar.protoweaver.api.protocol;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import lombok.*;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.auth.ClientAuthHandler;
import me.mrnavastar.protoweaver.api.auth.ServerAuthHandler;

import java.io.IOException;
import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Protocol {

    private final String name;
    private final List<Class<? extends ProtoPacket>> packets;
    private final Class<? extends ProtoConnectionHandler> serverHandler;
    private final Class<? extends ProtoConnectionHandler> clientHandler;
    private final Class<? extends ServerAuthHandler> serverAuthHandler;
    private final Class<? extends ClientAuthHandler> clientAuthHandler;
    private final CompressionType compression;
    private final int compressionLevel;
    private final Kryo kryo;

    @SneakyThrows
    public ProtoConnectionHandler newHandler(@NonNull Side side) throws NoSuchMethodException {
        return switch (side) {
            case CLIENT -> clientHandler.getDeclaredConstructor().newInstance();
            case SERVER -> serverHandler.getDeclaredConstructor().newInstance();
        };
    }

    @SneakyThrows
    public ServerAuthHandler newServerAuthHandler() {
        return serverAuthHandler.getDeclaredConstructor().newInstance();
    }

    @SneakyThrows
    public ClientAuthHandler newClientAuthHandler() {
        return clientAuthHandler.getDeclaredConstructor().newInstance();
    }

    public ByteBuf serialize(ProtoPacket packet) throws Exception {
        try (ByteBufOutputStream out = new ByteBufOutputStream(Unpooled.buffer())) {
            kryo.writeObject(new Output(out), packet);
            return out.buffer();
        }
    }

    public ProtoPacket deserialize(ByteBuf buf) throws IOException {
        try (ByteBufInputStream in = new ByteBufInputStream(buf)) {
            return (ProtoPacket) kryo.readClassAndObject(new Input(in));
        }
    }

    @Override
    public String toString() {
        return name;
    }
}