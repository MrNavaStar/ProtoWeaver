package me.mrnavastar.protoweaver.core.util;

import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.ProtoSerializer;
import me.mrnavastar.r.R;
import org.apache.fory.Fory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.Language;
import org.apache.fory.exception.InsecureException;
import org.apache.fory.logging.LoggerFactory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ObjectSerializer {

    private static class SerializerAdapter<T> extends Serializer<T> {

        private final ProtoSerializer<T> serializer;

        public SerializerAdapter(Fory fury, Class<T> type, ProtoSerializer<T> serializer) {
            super(fury, type);
            this.serializer = serializer;
        }

        @Override
        public T read(MemoryBuffer buffer) {
            ByteArrayInputStream in = new ByteArrayInputStream(buffer.getRemainingBytes());
            return serializer.read(in);
        }

        @Override
        public void write(MemoryBuffer buffer, T value) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            serializer.write(out, value);
            buffer.writeBytes(out.toByteArray());
        }
    }

    private final Fory fury = Fory.builder()
            .withJdkClassSerializableCheck(false)
            .withDeserializeNonexistentClass(false)
            .withLanguage(Language.JAVA)
            .withCompatibleMode(CompatibleMode.COMPATIBLE)
            .withAsyncCompilation(true)
            .withClassLoader(ProtoSerializer.class.getClassLoader())
            .build();

    static {
        // Make fury be quiet
        LoggerFactory.disableLogging();
    }

    private void recursiveRegister(Class<?> type, List<Class<?>> registered) {
        if (type == null || type == Object.class || registered.contains(type) || Modifier.isAbstract(type.getModifiers())) return;
        synchronized (fury) {
            fury.register(type);
        }
        registered.add(type);

        List.of(type.getDeclaredFields()).forEach(field -> recursiveRegister(field.getType(), registered));
        List.of(R.of(type).generics()).forEach(t -> recursiveRegister(t, registered));
        if (!type.isEnum()) recursiveRegister(type.getSuperclass(), registered);
    }

    public void register(Class<?> type) {
        recursiveRegister(type, new ArrayList<>());
    }

    @SneakyThrows
    public <T> void register(Class<T> type, ProtoSerializer<T> serializer) {
        synchronized (fury) {
            fury.registerSerializer(type, new SerializerAdapter<>(fury, type, serializer));
        }
    }

    public byte[] serialize(Object object) throws IllegalArgumentException {
        synchronized (fury) {
            try {
                return fury.serialize(object);
            } catch (InsecureException e) {
                throw new IllegalArgumentException("unregistered object: " + object.getClass().getName());
            }
        }
    }

    public Object deserialize(byte[] bytes) throws IllegalArgumentException {
        synchronized (fury) {
            try {
                return fury.deserialize(bytes);
            } catch (InsecureException e) {
                String packet = e.getMessage().split(" is not registered")[0].replace("class ", "");
                throw new IllegalArgumentException("unregistered object: " + packet);
            }
        }
    }
}