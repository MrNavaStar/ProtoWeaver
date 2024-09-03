package me.mrnavastar.protoweaver.loader.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.netty.NativeProtocol;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.util.List;

public class ProtoDeterminer extends ByteToMessageDecoder {

    private final boolean isSSL;

    static {
        ProtoWeaver.register(new NativeMinecraft());
        ProtoWeaver.register(new NativeProtoWeaver());
    }

    public ProtoDeterminer() {
        this.isSSL = false;
    }

    public ProtoDeterminer(boolean sslEnabled) {
        this.isSSL = sslEnabled;
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

        for (NativeProtocol protocol : ProtoWeaver.getNativeProtocols()) {
            if (!protocol.claim(magic1, magic2)) continue;

            if (protocol.resetPipe() && !isSSL) pipeline.toMap().forEach((key, value) -> {
                if (!key.equals("protoDeterminer")) pipeline.remove(value);
            });

            if (protocol.supportsSSL() && SSLContext.getContext() != null && enableSSL(buf)) {
                pipeline.addLast("ssl", SSLContext.getContext().newHandler(ctx.alloc()));
                pipeline.addLast("sslProtoDeterminer", new ProtoDeterminer(true));
                pipeline.remove(this);
                return;
            }

            protocol.start(ctx, buf, isSSL);
            pipeline.remove(this);
            return;
        }

        ctx.close();
    }

    private boolean enableSSL(ByteBuf buf) {
        if (isSSL) return false;
        return SslHandler.isEncrypted(buf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ProtoLogger.warn("Client rejected ssl certificate. Closing connection");
        ctx.close();
    }
}