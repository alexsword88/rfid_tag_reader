package com.yjjj.rfid;

import java.util.List;

import com.yjjj.rfid.RFIDReader.ReaderEvent;

import org.json.simple.JSONObject;

public class DetectProcess implements ReaderEvent {
    private RFIDReader reader;
    private RFIDWebSocketClient websocket;
    private String HAND_TAG = "ad2b05004a51d17c3e000012";
    private String LEG_TAG = "ad2b05004a50878141000004";

    public DetectProcess(String hostname, RFIDWebSocketClient websocket) {
        this.reader = new RFIDReader(hostname, DetectProcess.this);
        this.websocket = websocket;
        this.websocket.connect();
        // ROSpecParameter roSpecParameter = new ROSpecParameter(1000);
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
        // ROSpecParameter roSpecParameter = new ROSpecParameter(1000);
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

    @Override
    public void onTagDatasUpdate(List<TagData> tagDatas) {
        // TagData fakeTag = new TagData(this.LEG_TAG, 1);
        // int tagCount = 0;
        // int targetIndex = tagDatas.indexOf(fakeTag);
        // if (targetIndex != -1) {
        //     tagCount += 1;
        // }

        // fakeTag = new TagData(this.HAND_TAG, 1);
        // targetIndex = tagDatas.indexOf(fakeTag);
        // if (targetIndex != -1) {
        //     tagCount += 2;
        // }
        // if (tagCount > 1) {
        //     JSONObject jsonObj = new JSONObject();
        //     jsonObj.put("event", "triggerOn");
        //     this.websocket.send(jsonObj.toJSONString());
        // }
    }

}
