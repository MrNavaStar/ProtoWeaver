package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;

@Getter
@Setter
@AllArgsConstructor
public class ProtocolStatus {

    public enum Status {
        MISSING,
        MISMATCH,
        FULL,
        START,
        UPGRADE
    }

    private final String protoweaverVersion = ProtoConstants.PROTOWEAVER_VERSION;
    private String currentProtocol;
    private String nextProtocol;
    private byte[] nextSHA1;
    private Status status;
}