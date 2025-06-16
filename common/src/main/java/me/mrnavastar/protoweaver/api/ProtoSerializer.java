package me.mrnavastar.protoweaver.api;

import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public abstract class ProtoSerializer<T> extends Serializer<T> {

    public ProtoSerializer() {
        super(null, null);
        throw new RuntimeException("this class cannot be instantiated manually");
    }

    public ProtoSerializer(Fury fury, Class<T> type) {
        super(fury, type);
    }

    @Override
    public T read(MemoryBuffer buffer) {
        ByteArrayInputStream in = new ByteArrayInputStream(buffer.getRemainingBytes());
        return read(in);
    }

    @Override
    public void write(MemoryBuffer buffer, T value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, value);
        buffer.writeBytes(out.toByteArray());
    }

    public abstract T read(ByteArrayInputStream buffer);

    public abstract void write(ByteArrayOutputStream buffer, T value);

}