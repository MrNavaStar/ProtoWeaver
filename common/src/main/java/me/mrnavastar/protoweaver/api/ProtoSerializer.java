package me.mrnavastar.protoweaver.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public interface ProtoSerializer<T> {
    T read(ByteArrayInputStream buffer);
    void write(ByteArrayOutputStream buffer, T value);
}