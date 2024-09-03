package me.mrnavastar.protoweaver.loader.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.mrnavastar.protoweaver.api.netty.NativeProtocol;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.InternalConnectionHandler;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;

public class NativeProtoWeaver implements NativeProtocol {

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

        new ProtoConnection(InternalConnectionHandler.getProtocol(), Side.SERVER, ctx.channel());
        buf.readerIndex(2);
    }
}