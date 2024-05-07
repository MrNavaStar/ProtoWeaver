package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

public class InternalConnectionHandler {

    @Getter
    protected static final Protocol protocol = Protocol.create("protoweaver", "internal")
            .setServerHandler(ServerConnectionHandler.class)
            .setClientHandler(ClientConnectionHandler.class)
            .addPacket(AuthStatus.class)
            .addPacket(ProtocolStatus.class)
            .load();

    protected void disconnectIfNeverUpgraded(ProtoConnection connection, Sender sender) {
        if (!connection.getProtocol().toString().equals(protocol.toString())) return;
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
        Sender sender = connection.send(new ProtocolStatus(connection.getProtocol().toString(), name, 0, ProtocolStatus.Status.MISSING));
        disconnectIfNeverUpgraded(connection, sender);
    }
}