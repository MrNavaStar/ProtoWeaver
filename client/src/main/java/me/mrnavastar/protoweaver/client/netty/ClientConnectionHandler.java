package me.mrnavastar.protoweaver.client.netty;

import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.auth.AuthProvider;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.AuthStatus;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.InternalConnectionHandler;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ProtocolStatus;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;

public class ClientConnectionHandler extends InternalConnectionHandler implements ProtoConnectionHandler {

    private Protocol protocol;
    private boolean authenticated = false;
    private AuthProvider authHandler = null;

    public void start(ProtoConnection connection, Protocol protocol) {
        this.protocol = protocol;
        authenticated = false;
        if (protocol.requiresAuth(Side.CLIENT)) authHandler = protocol.newAuthProvider();
        connection.send(new ProtocolStatus(connection.getProtocol().toString(), protocol.toString(), protocol.getSHA1(), ProtocolStatus.Status.START));
    }

    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        if (packet instanceof ProtocolStatus status) {
            switch (status.getStatus()) {
                case MISSING -> {
                    protocol.logErr("Not loaded on server.");
                    disconnectIfNeverUpgraded(connection);
                }
                case MISMATCH -> {
                    protocol.logErr("Mismatch with protocol version on the server!");
                    protocol.logErr("Double check that all packets are registered in the same order and all settings are the same.");
                    disconnectIfNeverUpgraded(connection);
                }
                case FULL -> {
                    protocol.logErr("The maximum number of allowed connections on the server has been reached!");
                    disconnectIfNeverUpgraded(connection);
                }
                case UPGRADE -> {
                    if (!ProtoConstants.PROTOWEAVER_VERSION.equals(status.getProtoweaverVersion())) {
                        protocol.logWarn("Connecting with ProtoWeaver version: " + status.getProtoweaverVersion() + ", but server is running: " + ProtoConstants.PROTOWEAVER_VERSION + ". There could be unexpected issues.");
                    }

                    if (!authenticated) return;
                    protocol = ProtoWeaver.getLoadedProtocol(status.getNextProtocol());
                    if (protocol == null) {
                        protocolNotLoaded(connection, status.getNextProtocol());
                        return;
                    }
                    connection.upgradeProtocol(protocol);
                    protocol.logInfo("Connected to: " + connection.getRemoteAddress());
                }
            }
            return;
        }

        if (packet instanceof AuthStatus auth) {
            switch (auth) {
                case OK -> authenticated = true;
                case REQUIRED -> {
                    if (authHandler == null) {
                        protocol.logErr("Client protocol has not defined an auth handler, but the server requires auth. Closing connection.");
                        connection.disconnect();
                        return;
                    }
                    connection.send(authHandler.getSecret());
                }
                case DENIED -> {
                    protocol.logErr("Denied access by the server.");
                    disconnectIfNeverUpgraded(connection);
                }
            }
        }
    }
}
