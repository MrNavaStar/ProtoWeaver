package me.mrnavastar.protoweaver.loader.natives;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.netty.NativeProtocol;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

public class NativeHttp extends ChannelDuplexHandler implements NativeProtocol {

    public interface Server {
        void channelRead(ChannelHandlerContext ctx, ByteBuf msg) throws Exception;
    }

    private static final Pattern pattern = Pattern.compile("^https?:\\/\\/[^\\/]+\\/([^\\/?#]+)");
    private static final ConcurrentHashMap<String, Server> servers = new ConcurrentHashMap<>();

    static {
        ProtoWeaver.registerNative(new NativeHttp());
    }

    public static void registerServer(String endpoint, Server server) {
        servers.put(endpoint, server);
    }

    private final ConcurrentLinkedQueue<Server> handlers = new ConcurrentLinkedQueue<>();

    @Override
    public boolean claim(int magic1, int magic2) {
        return false;
    }

    @Override
    public void start(ChannelHandlerContext ctx, ByteBuf buf) {
        NativeHttp http = new NativeHttp();

        ctx.channel().pipeline().addLast(new HttpServerCodec());
        ctx.channel().pipeline().addLast(http);
        ctx.channel().pipeline().addLast(new HttpClientCodec());
        ctx.channel().pipeline().addLast(http);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (msg) {
            case HttpRequest request -> {
                String endpoint = pattern.matcher(request.uri()).group(1);
                if (endpoint == null) return;
                handlers.add(servers.get(endpoint));
                ctx.fireChannelRead(request);
            }
            case ByteBuf buf -> handlers.remove().channelRead(ctx, buf);
            default -> throw new IllegalStateException("Unexpected value: " + msg);
        }
    }
}