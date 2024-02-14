package me.mrnavastar.protoweaver.api;

import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

/**
 * A packet handler for your custom protocol.
 */
public interface ProtoConnectionHandler {
    /**
     * This function is called once the connection is ready to start sending/receiving packets.
     * @param connection The current connection.
     */
    default void onReady(ProtoConnection connection) {}

    /**
     * This function is called when the connection is closed.
     * @param connection The closed connection.
     */
    default void onDisconnect(ProtoConnection connection) {}

    /**
     * This function is called everytime a packet is received on your protocol.
     * @param connection The current connection.
     * @param packet The received object. use "instanceof" to check which one of your packets it is.
     */
    default void handlePacket(ProtoConnection connection, Object packet) {};
}