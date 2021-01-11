package com.yjjj.rfid;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException, URISyntaxException {
        RFIDWebSocketClient websocket = new RFIDWebSocketClient(new URI("ws://localhost:7749"));
        DetectProcess detectProcess = new DetectProcess("192.168.1.21", websocket);
        System.out.println(">>>>>> System Start ...");
        while (true) {
            detectProcess.startReadingTag();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            detectProcess.stopReadingTag();
        }
    }

}
