package me.mrnavastar.protoweaver.api;

import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class ProtoSerializer<T> {

    private static class SerializerWrapper<T> extends Serializer<T> {

        private final Function<ByteArrayInputStream, T> read;
        private final BiConsumer<ByteArrayOutputStream, T> write;

        public SerializerWrapper(Class<T> type, Function<ByteArrayInputStream, T> read, BiConsumer<ByteArrayOutputStream, T> write) {
            super(Fury.builder().build(), type);
            this.read = read;
            this.write = write;
        }

        @Override
        public T read(MemoryBuffer buffer) {
            ByteArrayInputStream in = new ByteArrayInputStream(buffer.getRemainingBytes());
            return read.apply(in);
        }

        @Override
        public void write(MemoryBuffer buffer, T value) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            write.accept(out, value);
            buffer.writeBytes(out.toByteArray());
        }
    }

    private final Serializer<T> serializer;

    public ProtoSerializer(Class<T> type) {
        serializer = new SerializerWrapper<>(type, this::read, this::write);
    }

    public abstract T read(ByteArrayInputStream buffer);

    public abstract void write(ByteArrayOutputStream buffer, T value);
}