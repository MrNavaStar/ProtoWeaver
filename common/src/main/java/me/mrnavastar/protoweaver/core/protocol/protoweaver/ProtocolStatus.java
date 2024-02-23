package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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

    private String currentProtocol;
    private String nextProtocol;
    private int nextProtocolHash;
    private Status status;
}