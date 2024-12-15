package me.mrnavastar.protoweaver.loader.natives;

import me.mrnavastar.protoweaver.api.netty.NativeProtocol;

public class NativeMinecraft implements NativeProtocol {

    // Check if packet is minecraft handshake - https://wiki.vg/Protocol#Handshaking
    @Override
    public boolean claim(int magic1, int magic2) {
        return magic1 > 0 && magic2 == 0;
    }

    @Override
    public boolean resetPipe() {
        return false;
    }

    @Override
    public boolean supportsSSL() {
        return false;
    }
}