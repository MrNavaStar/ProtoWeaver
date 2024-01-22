package me.mrnavastar.protoweaver.api.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompressionType {
    NONE(),
    GZIP(6),
    SNAPPY(),
    FAST_LZ(0);

    private final int defaultLevel;

    CompressionType() {
        this.defaultLevel = -1;
    }
}
