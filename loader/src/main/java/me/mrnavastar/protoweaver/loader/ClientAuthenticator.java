package me.mrnavastar.protoweaver.loader;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoAuthHandler;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;
import me.mrnavastar.protoweaver.protocol.protoweaver.ClientAuthResponse;
import me.mrnavastar.protoweaver.protocol.protoweaver.ServerAuthState;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.protocol.protoweaver.UpgradeProtocol;

public class ClientAuthenticator extends ProtoWeaver implements ProtoPacketHandler, ProtoAuthHandler {

    @Getter
    private static final Protocol serverProtocol = ProtoBuilder.protocol(protocol).setServerHandler(ClientAuthenticator.class).build();
    private boolean authenticated = false;
    private Protocol nextProtocol = null;

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof ClientAuthResponse auth) {
            authenticated = nextProtocol.newAuthHandler().handleAuth(auth.getSecret());
            if (!authenticated) {
                connection.send(new ServerAuthState(ServerAuthState.Status.DENIED));
                connection.disconnect();
                return;
            }
        }

        if (packet instanceof UpgradeProtocol upgrade) {
            nextProtocol = loadedProtocols.get(upgrade.getProtocol());
            if (nextProtocol == null) {
                protocolNotLoaded(upgrade.getProtocol());
                connection.disconnect();
                return;
            }

            if (nextProtocol.getAuthHandler() == null) {
                connection.send(new ServerAuthState(ServerAuthState.Status.OK));
                authenticated = true;
            } else connection.send(new ServerAuthState(ServerAuthState.Status.REQUIRED));
        }

        if (!authenticated) return;



        /*if (authenticated) {
            if (!(packet instanceof ProtocolRequest request)) return;

            // Switch back to internal Protocol
            if (!connection.getProtocol().equals(serverProtocol)) {
                connection.upgradeProtocol(serverProtocol, serverProtocol.newServerHandler());
                connection.send(new ProtocolRequest(serverProtocol.getName()));
            }

            Protocol newProtocol = loadedProtocols.get(request.getProtocol());
            if (newProtocol == null) {
                protocolNotLoaded(request.getProtocol());
                connection.disconnect();
                return;
            }



            return;
        }

        if (!(packet instanceof AuthRequest auth)) {
            connection.disconnect();
            return;
        }

        if (!FabricProxyLite.validate(auth.getSecret())) {
            connection.send(new AuthResponse(AuthResponse.Status.DENIED));
            connection.disconnect();
            return;
        }

        connection.send(new AuthResponse(AuthResponse.Status.OK));
        authenticated = true;
    }*/
    }

    @Override
    public boolean handleAuth(String key) {

        return false;
    }
}