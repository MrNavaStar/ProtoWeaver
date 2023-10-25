package me.mrnavastar.protoweaver.protocol;

import lombok.Getter;

@Getter
public enum CompressionType {
    NONE(0),
    GZIP(6),
    ZSTD(-1),
    BROTLI(-1);

    private final int defaultLevel;

    CompressionType(int defaultLevel) {
        this.defaultLevel = defaultLevel;
    }
}
