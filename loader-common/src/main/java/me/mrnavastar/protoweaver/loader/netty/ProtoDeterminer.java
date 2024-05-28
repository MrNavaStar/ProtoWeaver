package me.mrnavastar.protoweaver.loader.netty;

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

public class ProtoDeterminer extends ByteToMessageDecoder {

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
        /*if (!sslEnabled) {
            int size = pipeline.names().size() - 1;
            for (int i = 0; i < size; i++) pipeline.removeLast();
        }*/
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

        // Downstream protocol
        /*if (isHttp(magic1, magic2)) {
            pipeline.addLast("http", Http2Util.getAPNHandler());
            pipeline.remove(this);
        }*/
        /*if (isHttp(magic1, magic2)) {
            pipeline.addLast("httpDecoder", new HttpRequestDecoder());
            pipeline.addLast("httpEncoder", new HttpResponseEncoder());
            pipeline.addLast("compressor", new HttpContentCompressor());
            pipeline.addLast("httpHandler", new HttpHandler());
            pipeline.remove(this);
        }*/

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

    private boolean isHttp(int magic1, int magic2) {
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ProtoLogger.warn("Client rejected ssl certificate. Closing connection");
        ctx.close();
    }
}