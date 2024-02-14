package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class ProtocolStatus {

    public enum Status {
        MISSING,
        START,
        UPGRADE
    }

    private String currentProtocol;
    private String nextProtocol;
    private Status status;
}