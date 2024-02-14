package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import me.mrnavastar.protoweaver.api.ProtoWeaver;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.protocol.ProtoBuilder;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Protocol;

import me.mrnavastar.protoweaver.core.util.ProtoLogger;

public class InternalConnectionHandler extends ProtoWeaver {

    @Getter
    protected static final Protocol protocol = ProtoBuilder.protocol("protoweaver", "internal")
            .setServerHandler(ServerConnectionHandler.class)
            .setClientHandler(ClientConnectionHandler.class)
            .addPacket(AuthStatus.class)
            .addPacket(ProtocolStatus.class)
            .build();

    static {
        load(protocol);
    }

    protected void disconnectIfNeverUpgraded(ProtoConnection connection, Sender sender) {
        if (!connection.getProtocol().getName().equals(protocol.getName())) return;
        if (sender != null) {
            sender.disconnect();
            return;
        }
        connection.disconnect();
    }

    protected void disconnectIfNeverUpgraded(ProtoConnection connection) {
        disconnectIfNeverUpgraded(connection, null);
    }

    protected void protocolNotLoaded(ProtoConnection connection, String name) {
        ProtoLogger.warn("Protocol: " + name + " is not loaded! Closing connection!");
        Sender sender = connection.send(new ProtocolStatus(connection.getProtocol().getName(), name, ProtocolStatus.Status.MISSING));
        disconnectIfNeverUpgraded(connection, sender);
    }
}