package me.mrnavastar.protoweaver.api.netty;

import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.Sender;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;

import java.net.InetSocketAddress;

/**
 * This provider represents a connection to either a client or a server
 */
public interface ProtoConnection {

    /**
     * Checks if the connection is open.
     * @return True if open, false if closed.
     */
    boolean isOpen();

    /**
     * Closes the connection if it is open. Calling this function on a closed connection does nothing.
     */
    void disconnect();

    /**
     * Get the remote address of the connection. Check {@link InetSocketAddress} for more information.
     * @return {@link InetSocketAddress}
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Sends a {@link ProtoPacket} to the connected peer.
     * @return A {@link Sender} that can be used to close the connection after the packet is sent.
     */
    Sender send(ProtoPacket packet);

    /**
     * Changes the current connection protocol to the given protocol.
     * NOTE: You must call this on both the client and server or else you will have a protocol mismatch.
     * @param protocol The protocol the connection will switch to.
     */
    void upgradeProtocol(Protocol protocol);

    /**
     * Get the connections current protocol.
     * @return {@link Protocol}
     */
    Protocol getProtocol();

    /**
     * Get the side that this connection is on. Always returns {@link Side#CLIENT} on client and {@link Side#SERVER} on server.
     * @return {@link Side}
     */
    Side getSide();
}