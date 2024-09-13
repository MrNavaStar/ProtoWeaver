package me.mrnavastar.protoweaver.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProtoChannelHandler extends ByteToMessageDecoder {

    private static final Executor exec = Executors.newVirtualThreadPerTaskExecutor();
    private static final ConcurrentHashMap<String, Integer> connectionCount = new ConcurrentHashMap<>();

    private final ProtoConnection connection;
    @Setter
    private ProtoConnectionHandler handler;
    private ChannelHandlerContext ctx;
    private ByteBuf buf = Unpooled.buffer();

    public ProtoChannelHandler(ProtoConnection connection) {
        this.connection = connection;
        if (connection.getSide().equals(Side.CLIENT)) {
            buf.writeByte(0); // Fake out minecraft packet len
            buf.writeByte(ProtoConstants.PROTOWEAVER_MAGIC_BYTE);
        }
    }

    public static int getConnectionCount(Protocol protocol) {
        return connectionCount.getOrDefault(protocol.toString(), 0);
    }

    public static void incrementConnectionCount(Protocol protocol, int amount) {
        connectionCount.put(protocol.toString(), connectionCount.getOrDefault(protocol.toString(), 1) + amount);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            connectionCount.put(connection.getProtocol().toString(), connectionCount.getOrDefault(connection.getProtocol().toString(), 1) - 1);
            this.handler.onDisconnect(connection);
        } catch (Exception e) {
            connection.getProtocol().logErr("Threw an error on disconnect!");
            e.printStackTrace();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.readableBytes() == 0) return;

        byte[] bytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(bytes);

        Runnable runnable = () -> {
            Object packet = null;
            try {
                packet = connection.getProtocol().deserialize(bytes);
                handler.handlePacket(connection, packet);
            } catch (IllegalArgumentException e) {
                connection.getProtocol().logWarn("Ignoring an " + e.getMessage());
            } catch (Exception e) {
                if (packet != null) connection.getProtocol().logErr("Threw an error when trying to handle: " + packet.getClass() + "!");
                e.printStackTrace();
            }
        };

        if (connection.getProtocol().isAsync()) exec.execute(runnable);
        else runnable.run();
    }

    // Done with two bufs to prevent the user from messing with the internal data
    public Sender send(Object packet) {
        try {
            byte[] packetBuf = connection.getProtocol().serialize(packet);
            if (packetBuf.length == 0) return new Sender(connection, ctx.newSucceededFuture(), false);

            buf.writeInt(packetBuf.length); // Packet Len
            buf.writeBytes(packetBuf);

            Sender sender = new Sender(connection, ctx.writeAndFlush(buf), true);
            buf = Unpooled.buffer();
            return sender;

        } catch (IllegalArgumentException e) {
            connection.getProtocol().logErr("Tried to send an " + e.getMessage());
            return new Sender(connection, ctx.newSucceededFuture(), false);
        } catch (Exception e) {
            connection.getProtocol().logErr("Threw an error when trying to send: " + packet.getClass() + "!");
            e.printStackTrace();
            return new Sender(connection, ctx.newSucceededFuture(), false);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause.getMessage().contains("protoweaver-client-cert-error")) connection.disconnect();
    }
}