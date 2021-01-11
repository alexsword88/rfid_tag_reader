package com.yjjj.rfid;

import java.net.URI;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

public class RFIDWebSocketClient extends WebSocketClient {

    public RFIDWebSocketClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public RFIDWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    public RFIDWebSocketClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    @Override
    public void onError(Exception arg0) {
        arg0.printStackTrace();
    }

    @Override
    public void onMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connection Open");
    }

}
