package me.mrnavastar.protoweaver.api;

import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

/**
 * A packet handler for your custom protocol.
 */
public interface ProtoPacketHandler {
    /**
     * This function is called once the connection is ready to start receiving packets.
     * @param connection The current connection.
     */
    void ready(ProtoConnection connection);

    /**
     * This function is called everytime a packet is received on your protocol.
     * @param connection The current connection.
     * @param packet The received packet. use "instanceof" to check which one of your packets it is.
     */
    void handlePacket(ProtoConnection connection, ProtoPacket packet);
}