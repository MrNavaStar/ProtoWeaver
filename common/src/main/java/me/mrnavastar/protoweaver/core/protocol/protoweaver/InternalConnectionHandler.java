package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.util.Optional;

public class InternalConnectionHandler {

    @Getter
    protected static final Protocol protocol = Protocol.create("protoweaver", "internal")
            .addPacket(AuthStatus.class)
            .addPacket(ProtocolStatus.class)
            .build();

    protected void disconnectIfNeverUpgraded(ProtoConnection connection, Sender sender) {
        if (!connection.getProtocol().toString().equals(protocol.toString())) return;

        Optional.ofNullable(sender).ifPresentOrElse(Sender::disconnect, connection::disconnect);
        connection.getProtocol().logWarn("Refusing connection from: " + connection.getRemoteAddress());
    }

    protected void disconnectIfNeverUpgraded(ProtoConnection connection) {
        disconnectIfNeverUpgraded(connection, null);
    }

    protected void protocolNotLoaded(ProtoConnection connection, String name) {
        ProtoLogger.warn("Protocol: " + name + " is not loaded! Closing connection!");
        Sender sender = connection.send(new ProtocolStatus(connection.getProtocol().toString(), name, 0, ProtocolStatus.Status.MISSING));
        disconnectIfNeverUpgraded(connection, sender);
    }
}