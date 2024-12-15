package me.mrnavastar.protoweaver.loader.natives;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import me.mrnavastar.protoweaver.api.netty.NativeProtocol;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.InternalConnectionHandler;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import me.mrnavastar.protoweaver.loader.netty.ServerConnectionHandler;

public class NativeProtoWeaver implements NativeProtocol {

    private static final Protocol INTERNAL = InternalConnectionHandler.PROTOCOL.setHandler(ServerConnectionHandler.class).load();
    private boolean ssl = false;

    @Override
    public boolean claim(int magic1, int magic2) {
        return magic1 == 0 && magic2 == ProtoConstants.PROTOWEAVER_MAGIC_BYTE;
    }

    private boolean enableSSL(ByteBuf buf) {
        if (ssl) return false;
        return SslHandler.isEncrypted(buf);
    }

    @Override
    public void start(ChannelHandlerContext ctx, ByteBuf buf) {
        if (SSLContext.getContext() != null && enableSSL(buf)) {
            ctx.channel().pipeline().addLast("ssl", SSLContext.getContext().newHandler(ctx.alloc()));
            /*ctx.channel().pipeline().addLast("sslProtoDeterminer", new ProtoDeterminer(true));
            ctx.channel().pipeline().remove(this);*/
            return;
        }

        new ProtoConnection(INTERNAL, Side.SERVER, ctx.channel());
        buf.readerIndex(2);
    }
}