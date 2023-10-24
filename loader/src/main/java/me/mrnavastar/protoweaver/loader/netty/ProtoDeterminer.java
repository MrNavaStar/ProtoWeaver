package me.mrnavastar.protoweaver.loader.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import me.mrnavastar.protoweaver.util.ProtoConstants;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtoWeaver;

import java.util.List;

public class ProtoDeterminer extends ByteToMessageDecoder {

    private final boolean sslEnabled;
    private final boolean gzipEnabled;
    private static final String[] minecraftHandlers = {
            "timeout",
            "legacy_query",
            "splitter",
            "decoder",
            "prepender",
            "encoder",
            "unbundler",
            "bundler",
            "packet_handler"
    };

    public ProtoDeterminer() {
        this.sslEnabled = false;
        this.gzipEnabled = false;
    }

    public ProtoDeterminer(boolean sslEnabled, boolean gzipEnabled) {
        this.sslEnabled = sslEnabled;
        this.gzipEnabled = gzipEnabled;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
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
        if (!(sslEnabled || gzipEnabled)) for (String handler : minecraftHandlers) pipeline.remove(handler);

        // Upstream protocol
        if (SSLContext.context != null && enableSSL(buf)) {
            pipeline.addLast("ssl", SSLContext.context.newHandler(ctx.alloc()));
            pipeline.addLast("sslProtoDeterminer", new ProtoDeterminer(true, gzipEnabled));
            pipeline.remove(this);
            return;
        }

        // Upstream protocol
        if (enableGzip(magic1, magic2)) {
            pipeline.addLast("compressionEncoder", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
            pipeline.addLast("compressionDecoder", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
            pipeline.addLast("gzipProtoDeterminer", new ProtoDeterminer(sslEnabled, true));
            pipeline.remove(this);
            return;
        }
        // Downstream protocol
        if (isProtoWeaver(magic1, magic2)) {
            Protocol internal = ProtoWeaver.getProtocol();
            ProtoConnection connection = new ProtoConnection(internal, internal.newServerHandler(), pipeline);
            buf.readerIndex(2);
            pipeline.remove(this);
            return;
        }

        // Downstream protocol
        if (isHttp(magic1, magic2)) {
            pipeline.addLast("httpDecoder", new HttpRequestDecoder());
            pipeline.addLast("httpEncoder", new HttpResponseEncoder());
            pipeline.addLast("compressor", new HttpContentCompressor());
            pipeline.addLast("httpHandler", new HttpHandler());
            pipeline.remove(this);
        }
    }

    // Check if packet is minecraft handshake - https://wiki.vg/Protocol#Handshaking
    private boolean isMinecraft(int magic1, int magic2) {
        return magic1 > 0 && magic2 == 0;
    }

    private boolean enableSSL(ByteBuf buf) {
        if (sslEnabled) return false;
        return SslHandler.isEncrypted(buf);
    }

    private boolean enableGzip(int magic1, int magic2) {
        if (gzipEnabled) return false;
        return magic1 == 31 && magic2 == 139;
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
}