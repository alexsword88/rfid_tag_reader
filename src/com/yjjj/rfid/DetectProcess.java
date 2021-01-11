package com.yjjj.rfid;

import com.yjjj.rfid.RFIDReader.ReaderEvent;

public class DetectProcess implements ReaderEvent {
    private RFIDReader reader;
    private RFIDWebSocketClient websocket;

    public DetectProcess(String hostname, RFIDWebSocketClient websocket) {
        this.reader = new RFIDReader(hostname, DetectProcess.this);
        this.websocket = websocket;
        this.websocket.connect();
        ROSpecParameter roSpecParameter = new ROSpecParameter(-1);
        this.reader.setROSpecs(roSpecParameter);
        if (!this.reader.connect()) {
            System.exit(1);
        }
    }

    public DetectProcess(String hostname, int port, RFIDWebSocketClient websocket) {
        this.reader = new RFIDReader(hostname, port, DetectProcess.this);
        this.websocket = websocket;
        this.websocket.connect();
        ROSpecParameter roSpecParameter = new ROSpecParameter(-1);
        this.reader.setROSpecs(roSpecParameter);
        if (!this.reader.connect()) {
            System.exit(1);
        }
    }

    public void startReadingTag() {
        this.reader.run();
    }

    public void stopReadingTag() {
        this.reader.stop();
    }

    public void disconnect() {
        this.reader.disconnect();
    }

    @Override
    public void onTagDataUpdate(TagData tagData) {
        this.websocket.send(tagData.toJSONString());
    }
}
