package me.mrnavastar.protoweaver.api.protocol.velocity;

import me.mrnavastar.protoweaver.api.auth.AuthProvider;
import me.mrnavastar.protoweaver.api.auth.Authenticator;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import lombok.Setter;

import java.util.Arrays;

public class VelocityAuth implements Authenticator, AuthProvider {

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