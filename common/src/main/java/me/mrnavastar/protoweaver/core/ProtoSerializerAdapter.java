package me.mrnavastar.protoweaver.core;

import me.mrnavastar.protoweaver.api.ProtoSerializer;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;

public class ProtoSerializerAdapter<T> extends Serializer<T> {

    private final ProtoSerializer<T> streamSerializer;

    public ProtoSerializerAdapter(Fury fury, Class<T> type, Class<? extends ProtoSerializer<?>> streamSerializer) {
        super(fury, type);
        try {
            this.streamSerializer = (ProtoSerializer<T>) streamSerializer.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T read(MemoryBuffer buffer) {
        ByteArrayInputStream in = new ByteArrayInputStream(buffer.getRemainingBytes());
        return streamSerializer.read(in);
    }

    @Override
    public void write(MemoryBuffer buffer, T value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamSerializer.write(out, value);
        buffer.writeBytes(out.toByteArray());
    }
}
