package me.mrnavastar.protoweaver.loader.protocol.protoweaver;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.api.ServerAuthHandler;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;
import me.mrnavastar.protoweaver.protocol.protoweaver.AuthStatus;
import me.mrnavastar.protoweaver.protocol.protoweaver.ClientSecret;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtocolStatus;

public class ServerHandler extends ProtoWeaver implements ProtoPacketHandler {

    @Getter
    private static final Protocol serverProtocol = ProtoBuilder.protocol(baseProtocol)
            .setServerHandler(ServerHandler.class)
            .build();

    private boolean authenticated = false;
    private Protocol nextProtocol = null;
    private ServerAuthHandler authHandler = null;

    static {
        load(serverProtocol);
    }

    @Override
    public void ready(ProtoConnection connection) {

    }

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof ProtocolStatus upgrade && upgrade.getStatus().equals(ProtocolStatus.Status.START)) {
            // Check if protocol loaded
            nextProtocol = loadedProtocols.get(upgrade.getProtocol());
            if (nextProtocol == null) {
                protocolNotLoaded(upgrade.getProtocol());
                connection.send(new ProtocolStatus(nextProtocol.getName(), ProtocolStatus.Status.MISSING)).disconnect();
                return;
            }

            // Check if protocol needs authentication
            if (nextProtocol.getServerAuthHandler() != null) {
                authHandler = nextProtocol.newServerAuthHandler();
                connection.send(new AuthStatus(AuthStatus.Status.REQUIRED));
                return;
            }

            authenticated = true;
        }

        // Authenticate client
        if (nextProtocol != null && packet instanceof ClientSecret auth) {
            authenticated = authHandler.handleAuth(connection, auth.getSecret());
        }

        if (!authenticated) {
            connection.send(new AuthStatus(AuthStatus.Status.DENIED)).disconnect();
            return;
        }

        // Upgrade protocol
        connection.send(new AuthStatus(AuthStatus.Status.OK));
        connection.send(new ProtocolStatus(nextProtocol.getName(), ProtocolStatus.Status.UPGRADE));
        connection.upgradeProtocol(nextProtocol);
    }
}