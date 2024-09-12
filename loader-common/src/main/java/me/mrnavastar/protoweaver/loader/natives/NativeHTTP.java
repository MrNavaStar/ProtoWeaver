package me.mrnavastar.protoweaver.loader.natives;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.mrnavastar.protoweaver.api.netty.NativeProtocol;

public class NativeHTTP implements NativeProtocol {

    @Override
    public boolean claim(int magic1, int magic2) {
        return
            magic1 == 'G' && magic2 == 'E' || // GET
            magic1 == 'P' && magic2 == 'O' || // POST
            magic1 == 'P' && magic2 == 'U' || // PUT
            magic1 == 'H' && magic2 == 'E' || // HEAD
            magic1 == 'O' && magic2 == 'P' || // OPTIONS
            magic1 == 'P' && magic2 == 'A' || // PATCH
            magic1 == 'D' && magic2 == 'E' || // DELETE
            magic1 == 'T' && magic2 == 'R' || // TRACE
            magic1 == 'C' && magic2 == 'O';   // CONNECT
    }

    @Override
    public void start(ChannelHandlerContext ctx, ByteBuf buf, boolean isSSL) {

    }
}