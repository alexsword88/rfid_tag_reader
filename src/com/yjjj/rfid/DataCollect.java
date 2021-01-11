package com.yjjj.rfid;

import java.util.ArrayList;
import java.util.List;

import com.yjjj.rfid.RFIDReader.ReaderEvent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DataCollect implements ReaderEvent {
    private RFIDReader reader;
    private int anttenaMaxPort = Const.ANTENNA_MAX_PORT_IPJ_R420;
    private String HAND_TAG = "ad2b05004a51d17c3e000012";
    private String LEG_TAG = "ad2b05004a50878141000004";
    private JSONWriter handTagWritter;
    private JSONArray totalHandArray = new JSONArray();
    private JSONArray handArray = new JSONArray();
    private JSONWriter legTagWritter;
    private JSONArray totalLegArray = new JSONArray();
    private JSONArray legArray = new JSONArray();
    private List<TagData> sessionTags = new ArrayList<TagData>();

    public DataCollect(String hostname, String fileState) {
        this.reader = new RFIDReader(hostname, DataCollect.this);
        // ROSpecParameter roSpecParameter = new ROSpecParameter(1000);
        this.handTagWritter = new JSONWriter(String.format("exp/hand/State-%s.json", fileState));
        this.legTagWritter = new JSONWriter(String.format("exp/leg/State-%s.json", fileState));
        ROSpecParameter roSpecParameter = new ROSpecParameter(-1);
        this.reader.setROSpecs(roSpecParameter);
    }

    public DataCollect(String hostname, int port, String fileState) {
        this.reader = new RFIDReader(hostname, port, DataCollect.this);
        // ROSpecParameter roSpecParameter = new ROSpecParameter(1000);
        this.handTagWritter = new JSONWriter(String.format("exp/hand/State-%s.json", fileState));
        this.legTagWritter = new JSONWriter(String.format("exp/leg/State-%s.json", fileState));
        ROSpecParameter roSpecParameter = new ROSpecParameter(-1);
        this.reader.setROSpecs(roSpecParameter);
    }

    public void changeAntennaMaxPort(int maxPort) {
        this.anttenaMaxPort = maxPort;
    }

    @Override
    public void onTagDataUpdate(TagData tagData) {
        if (tagData.epc.equals("ad2b05004a51d17c3e000012") || tagData.epc.equals("ad2b05004a50878141000004")) {
            tagData.descriptions();
            this.sessionTags.add(tagData);
        }
    }

    @Override
    public void onTagDatasUpdate(List<TagData> tagDatas) {
        TagData fakeTag = new TagData(this.HAND_TAG, 1);
        TagData lastTag = null;
        int tagCount = 0;
        int targetIndex = this.sessionTags.indexOf(fakeTag);
        if (targetIndex != -1) {
            tagCount += 1;
        }

        fakeTag = new TagData(this.LEG_TAG, 1);
        lastTag = null;
        // while (true) {
        // int targetIndex = this.sessionTags.indexOf(fakeTag);
        // if (targetIndex != -1) {
        // lastTag = this.sessionTags.remove(targetIndex);
        // } else {
        // break;
        // }
        // }
        // legTag.add(lastTag);
    }

    public void startReadingTag() {
        if (!this.reader.connect()) {
            System.exit(1);
        }
        this.reader.run();
    }

    public void stopReadingTag() {
        this.reader.stop();

        List<TagData> handTag = new ArrayList<TagData>();
        List<TagData> legTag = new ArrayList<TagData>();
        for (int i = 0; i < this.anttenaMaxPort; i++) {
            TagData fakeTag = new TagData(this.HAND_TAG, i + 1);
            TagData lastTag = null;
            while (true) {
                int targetIndex = this.sessionTags.indexOf(fakeTag);
                if (targetIndex != -1) {
                    lastTag = this.sessionTags.remove(targetIndex);
                } else {
                    break;
                }
            }
            handTag.add(lastTag);

            fakeTag = new TagData(this.LEG_TAG, i + 1);
            lastTag = null;
            while (true) {
                int targetIndex = this.sessionTags.indexOf(fakeTag);
                if (targetIndex != -1) {
                    lastTag = this.sessionTags.remove(targetIndex);
                } else {
                    break;
                }
            }
            legTag.add(lastTag);
        }
        if (this.handTagWritter != null) {
            for (TagData tagdata : handTag) {
                if (tagdata == null) {
                    this.handArray.add(null);
                    continue;
                }
                JSONObject object = new JSONObject();
                object.put("lastSeenUTC", tagdata.lastSeenUTC.toString());
                object.put("antennaID", tagdata.antennaID);
                object.put("epc", tagdata.epc);
                object.put("rssi", tagdata.peakRssi);
                object.put("count", tagdata.seenCount);
                this.handArray.add(object);
            }
        }
        if (this.legTagWritter != null) {
            for (TagData tagdata : legTag) {
                if (tagdata == null) {
                    this.legArray.add(null);
                    continue;
                }
                JSONObject object = new JSONObject();
                object.put("lastSeenUTC", tagdata.lastSeenUTC.toString());
                object.put("antennaID", tagdata.antennaID);
                object.put("epc", tagdata.epc);
                object.put("rssi", tagdata.peakRssi);
                object.put("count", tagdata.seenCount);
                this.legArray.add(object);
            }
        }
        this.totalHandArray.add(this.handArray);
        this.totalLegArray.add(this.legArray);
        this.sessionTags = new ArrayList<TagData>();
        this.handArray = new JSONArray();
        this.legArray = new JSONArray();
    }

    public void disconnect() {
        this.reader.disconnect();
        if (this.handTagWritter != null) {
            this.handTagWritter.write(this.totalHandArray);
            this.handTagWritter.save();
        }
        if (this.legTagWritter != null) {
            this.legTagWritter.write(this.totalLegArray);
            this.legTagWritter.save();
        }
    }
}
