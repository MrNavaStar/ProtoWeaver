package me.mrnavastar.protoweaver.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.InternalConnectionHandler;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.util.List;
import java.util.Map;

public class  ProtoDeterminer extends ByteToMessageDecoder {

    private final boolean sslEnabled;

    public ProtoDeterminer() {
        this.sslEnabled = false;
    }

    public ProtoDeterminer(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public static void registerToPipeline(Channel channel) {
        channel.pipeline().addFirst("protoDeterminer", new ProtoDeterminer());
    }

    @Override
    @SneakyThrows
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) {
        if (buf.readableBytes() < 5) {
            return;
        }

        ChannelPipeline pipeline = ctx.pipeline();
        int magic1 = buf.getUnsignedByte(buf.readerIndex());
        int magic2 = buf.getUnsignedByte(buf.readerIndex() + 1);

        if (isMinecraft(magic1, magic2)) {
            pipeline.remove(this);
            return;
        }

        // Not a player - clear the pipeline
        if (!sslEnabled) {
            for (Map.Entry<String, ChannelHandler> handler : pipeline.toMap().entrySet()) {
                if (handler.getKey().equals("protoDeterminer")) continue;
                pipeline.remove(handler.getValue());
            }
        }

        // Upstream protocol
        if (SSLContext.getContext() != null && enableSSL(buf)) {
            pipeline.addLast("ssl", SSLContext.getContext().newHandler(ctx.alloc()));
            pipeline.addLast("sslProtoDeterminer", new ProtoDeterminer(true));
            pipeline.remove(this);
            return;
        }

        // Downstream protocol
        if (isProtoWeaver(magic1, magic2)) {
            // Enforce ssl. Not sure if this should be configurable
            if (!sslEnabled) {
                ctx.close();
                return;
            }

            new ProtoConnection(InternalConnectionHandler.getProtocol(), Side.SERVER, ctx.channel());
            buf.readerIndex(2);
            pipeline.remove(this);
            return;
        }

        ctx.close();
    }

    // Check if packet is minecraft handshake - https://wiki.vg/Protocol#Handshaking
    private boolean isMinecraft(int magic1, int magic2) {
        return magic1 > 0 && magic2 == 0;
    }

    private boolean enableSSL(ByteBuf buf) {
        if (sslEnabled) return false;
        return SslHandler.isEncrypted(buf);
    }

    private boolean isProtoWeaver(int magic1, int magic2) {
        return magic1 == 0 && magic2 == ProtoConstants.PROTOWEAVER_MAGIC_BYTE;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ProtoLogger.warn("Client rejected ssl certificate. Closing connection");
        ctx.close();
    }
}