package me.mrnavastar.protoweaver.api.auth;

/**
 * A simple provider class for client authentication. Any implementations loaded on the server won't do anything.
 */
public interface ClientAuthHandler {

    /**
     * This function is called on the client when it needs to send it's secret to the server for authentication.
     * @return The secret that will be sent to the server.
     */
    String getSecret();
}