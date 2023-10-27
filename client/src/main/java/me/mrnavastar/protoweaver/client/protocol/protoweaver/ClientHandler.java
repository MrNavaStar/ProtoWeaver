package me.mrnavastar.protoweaver.client.protocol.protoweaver;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;
import me.mrnavastar.protoweaver.protocol.protoweaver.AuthStatus;
import me.mrnavastar.protoweaver.protocol.protoweaver.ClientSecret;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtocolStatus;

public class ClientHandler extends ProtoWeaver implements ProtoPacketHandler {

    @Getter
    private static final Protocol clientProtocol = ProtoBuilder.protocol(baseProtocol).setClientHandler(ClientHandler.class).build();
    private boolean authenticated = false;

    @Override
    public void ready(ProtoConnection connection) {
        connection.send(new ProtocolStatus("???", ProtocolStatus.Status.UPGRADE));
    }

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof ProtocolStatus status) {
            switch (status.getStatus()) {
                case MISSING -> {
                    // Protocol is missing on server
                    connection.disconnect();
                }
                case UPGRADE -> {
                    if (!authenticated) return;
                    connection.upgradeProtocol();
                }
            }
            return;
        }

        if (packet instanceof AuthStatus auth) {
            switch (auth.getStatus()) {
                case OK -> {
                    authenticated = true;
                }
                case REQUIRED -> {
                    connection.send(new ClientSecret("????????????"));
                }
                case DENIED -> {
                    // This client is not authorized to connect on this protocol
                    connection.disconnect();
                }
            }
        }
    }
}
