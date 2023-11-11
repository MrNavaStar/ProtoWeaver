package me.mrnavastar.protoweaver.client.protocol.protoweaver;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.auth.ClientAuthHandler;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.AuthStatus;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ProtocolStatus;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ClientSecret;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

public class ClientHandler extends ProtoWeaver implements ProtoPacketHandler {

    @Getter
    private static final Protocol clientProtocol = ProtoBuilder.protocol(baseProtocol).setClientHandler(ClientHandler.class).build();
    private boolean authenticated = false;
    private ClientAuthHandler authHandler = null;

    static {
        load(clientProtocol);
    }

    @Override
    public void onReady(ProtoConnection c) {
        me.mrnavastar.protoweaver.core.netty.ProtoConnection connection = (me.mrnavastar.protoweaver.core.netty.ProtoConnection) c;

        if (connection.getNext().getClientAuthHandler() != null) authHandler = connection.getNext().newClientAuthHandler();
        connection.send(new ProtocolStatus(connection.getNext().getName(), ProtocolStatus.Status.START));
    }

    @Override
    public void handlePacket(ProtoConnection c, ProtoPacket packet) {
        me.mrnavastar.protoweaver.core.netty.ProtoConnection connection = (me.mrnavastar.protoweaver.core.netty.ProtoConnection) c;

        if (packet instanceof ProtocolStatus status) {
            switch (status.getStatus()) {
                case MISSING -> {
                    ProtoLogger.error("Protocol is not loaded on server. Closing connection.");
                    connection.disconnect();
                }
                case UPGRADE -> {
                    if (!authenticated) return;
                    if (!connection.getNext().getName().equals(status.getProtocol())) {
                        protocolNotLoaded(status.getProtocol());
                        connection.send(new ProtocolStatus(status.getProtocol(), ProtocolStatus.Status.MISSING)).disconnect();
                        return;
                    }

                    connection.upgradeProtocol(connection.getNext());
                }
            }
            return;
        }

        if (packet instanceof AuthStatus auth) {
            switch (auth.getStatus()) {
                case OK -> authenticated = true;
                case REQUIRED -> {
                    if (authHandler == null) {
                        ProtoLogger.error("Client protocol has not defined an auth handler, but the server requires auth. Closing connection.");
                        connection.disconnect();
                        return;
                    }
                    connection.send(new ClientSecret(authHandler.getSecret()));
                }
                case DENIED -> {
                    ProtoLogger.error("Client was denied access by the server. Closing connection.");
                    connection.disconnect();
                }
            }
        }
    }
}
