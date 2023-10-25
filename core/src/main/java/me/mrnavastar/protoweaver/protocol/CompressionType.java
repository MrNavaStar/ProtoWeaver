package me.mrnavastar.protoweaver.protocol;

import lombok.Getter;

@Getter
public enum CompressionType {
    NONE(-1),
    GZIP(6),
    SNAPPY(-1),
    BROTLI(-1),
    LZ4(-1);

    private final int defaultLevel;

    CompressionType(int defaultLevel) {
        this.defaultLevel = defaultLevel;
    }
}
