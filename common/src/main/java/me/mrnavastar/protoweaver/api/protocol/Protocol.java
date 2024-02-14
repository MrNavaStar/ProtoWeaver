package me.mrnavastar.protoweaver.api.protocol;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.auth.ClientAuthHandler;
import me.mrnavastar.protoweaver.api.auth.ServerAuthHandler;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Protocol {

    private final String name;
    private final Kryo kryo;
    private final CompressionType compression;
    private final int compressionLevel;
    private final int maxPacketSize;

    @Setter
    private Class<? extends ProtoConnectionHandler> serverHandler;
    @Setter
    private Class<? extends ProtoConnectionHandler> clientHandler;
    @Setter
    private Class<? extends ServerAuthHandler> serverAuthHandler;
    @Setter
    private Class<? extends ClientAuthHandler> clientAuthHandler;

    public byte[] serialize(Object packet) {
        try (Output output = new Output(maxPacketSize)) {
            kryo.writeClassAndObject(output, packet);
            return output.toBytes();
        }
    }

    public Object deserialize(byte[] packet) {
        try (Input in = new Input(packet)) {
            return kryo.readClassAndObject(in);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}