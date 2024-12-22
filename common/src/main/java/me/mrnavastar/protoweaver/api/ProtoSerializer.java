package me.mrnavastar.protoweaver.api;

import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class ProtoSerializer<T> {

    private record FunctionWrapper<T>(Function<ByteArrayInputStream, T> read, BiConsumer<ByteArrayOutputStream, T> write) { }

    public static class SerializerWrapper<T> extends Serializer<T> {

        private static final ConcurrentHashMap<Class<?>, FunctionWrapper<?>> serializers = new ConcurrentHashMap<>();
        private final FunctionWrapper<T> wrapper;

        public SerializerWrapper(Fury fury, Class<T> type) {
            super(fury, type);
            this.wrapper = (FunctionWrapper<T>) serializers.remove(type);
        }

        @Override
        public T read(MemoryBuffer buffer) {
            ByteArrayInputStream in = new ByteArrayInputStream(buffer.getRemainingBytes());
            return wrapper.read.apply(in);
        }

        @Override
        public void write(MemoryBuffer buffer, T value) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wrapper.write.accept(out, value);
            buffer.writeBytes(out.toByteArray());
        }
    }

    public ProtoSerializer(Class<T> type) {
        SerializerWrapper.serializers.put(type, new FunctionWrapper<>(this::read, this::write));
    }

    public abstract T read(ByteArrayInputStream buffer);

    public abstract void write(ByteArrayOutputStream buffer, T value);
}