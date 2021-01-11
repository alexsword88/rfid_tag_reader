package com.yjjj.rfid;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class RFIDWebsocket extends WebSocketServer {

    public RFIDWebsocket(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public RFIDWebsocket(InetSocketAddress address) {
        super(address);
    }

    public RFIDWebsocket(int port, Draft_6455 draft) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
    }

    @Override
    public void onClose(org.java_websocket.WebSocket arg0, int arg1, String arg2, boolean arg3) {

    }

    @Override
    public void onError(org.java_websocket.WebSocket arg0, Exception arg1) {

    }

    @Override
    public void onMessage(org.java_websocket.WebSocket arg0, String arg1) {

    }

    @Override
    public void onOpen(org.java_websocket.WebSocket arg0, ClientHandshake arg1) {

    }

    @Override
    public void onStart() {

    }
}
