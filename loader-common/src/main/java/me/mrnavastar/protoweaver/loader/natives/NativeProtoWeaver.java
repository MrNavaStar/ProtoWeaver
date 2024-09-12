package me.mrnavastar.protoweaver.loader.natives;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.mrnavastar.protoweaver.api.netty.NativeProtocol;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.InternalConnectionHandler;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.loader.netty.ServerConnectionHandler;

public class NativeProtoWeaver implements NativeProtocol {

    private final Protocol INTERNAL = InternalConnectionHandler.getProtocol().modify().setHandler(ServerConnectionHandler.class).load();

    @Override
    public boolean claim(int magic1, int magic2) {
        return magic1 == 0 && magic2 == ProtoConstants.PROTOWEAVER_MAGIC_BYTE;
    }

    @Override
    public void start(ChannelHandlerContext ctx, ByteBuf buf, boolean isSSL) {
        if (!isSSL) {
            ctx.close();
            return;
        }

        new ProtoConnection(INTERNAL, Side.SERVER, ctx.channel());
        buf.readerIndex(2);
    }
}