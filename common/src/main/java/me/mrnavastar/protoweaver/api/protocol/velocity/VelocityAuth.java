package me.mrnavastar.protoweaver.api.protocol.velocity;

import me.mrnavastar.protoweaver.api.auth.ClientAuthHandler;
import me.mrnavastar.protoweaver.api.auth.ServerAuthHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

public class VelocityAuth implements ServerAuthHandler, ClientAuthHandler {

    @Setter
    private static byte[] secret = null;

    @Override
    public boolean handleAuth(ProtoConnection connection, byte[] key) {
        return Arrays.equals(key, secret);
    }

    @Override
    public byte[] getSecret() {
        return secret;
    }
}