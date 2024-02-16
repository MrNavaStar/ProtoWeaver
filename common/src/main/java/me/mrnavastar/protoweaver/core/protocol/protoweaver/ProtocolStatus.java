package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProtocolStatus {

    public enum Status {
        MISSING,
        MISMATCH,
        START,
        UPGRADE
    }

    private String currentProtocol;
    private String nextProtocol;
    private int nextProtocolHash;
    private Status status;
}