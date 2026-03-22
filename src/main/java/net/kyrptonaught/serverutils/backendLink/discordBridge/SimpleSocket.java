package net.kyrptonaught.serverutils.backendLink.discordBridge;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public abstract class SimpleSocket extends WebSocketClient {
    public SimpleSocket(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
