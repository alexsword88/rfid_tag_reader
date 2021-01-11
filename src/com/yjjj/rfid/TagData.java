package com.yjjj.rfid;

import java.util.List;

import org.json.simple.JSONObject;
import org.llrp.ltk.generated.enumerations.C1G2ReadResultType;
import org.llrp.ltk.generated.interfaces.AccessCommandOpSpecResult;
import org.llrp.ltk.generated.interfaces.AirProtocolTagData;
import org.llrp.ltk.generated.parameters.C1G2LockOpSpecResult;
import org.llrp.ltk.generated.parameters.C1G2ReadOpSpecResult;
import org.llrp.ltk.generated.parameters.C1G2WriteOpSpecResult;
import org.llrp.ltk.types.UnsignedLong_DATETIME;

public class TagData {
    int seenCount;
    int peakRssi;
    String epc;
    int antennaID;
    UnsignedLong_DATETIME lastSeenUTC;
    int channelIndex;
    int accessSpecID;
    List<AccessCommandOpSpecResult> accessCommandOpSpecResultList;
    List<AirProtocolTagData> airProtocolTagDataList;

    public TagData(String epc, int antennaID, int peakRssi, UnsignedLong_DATETIME lastSeenUTC, int seenCount,
            int channelIndex, int accessSpecID, List<AccessCommandOpSpecResult> accessCOpSpecResult,
            List<AirProtocolTagData> airProtocolTagData) {
        this.epc = epc;
        this.antennaID = antennaID;
        this.peakRssi = peakRssi;
        this.lastSeenUTC = lastSeenUTC;
        this.seenCount = seenCount;
        this.channelIndex = channelIndex;
        this.accessSpecID = accessSpecID;
        this.accessCommandOpSpecResultList = accessCOpSpecResult;
        this.airProtocolTagDataList = airProtocolTagData;
    }

    // For filter tag
    public TagData(String epc, int antennaID) {
        this.epc = epc;
        this.antennaID = antennaID;
    }

    public String toJSONString() {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();

        jsonObject.put("event", "onReadTag");
        data.put("time", this.lastSeenUTC.toString());
        data.put("epc", this.epc);
        data.put("aid", this.antennaID);
        data.put("rssi", this.peakRssi);
        data.put("count", this.seenCount);
        jsonObject.put("data", data);

        return jsonObject.toJSONString();
    }

    public void descriptions() {
        System.out.println(String.format("%-10s:%s", "UTCTime", this.lastSeenUTC.toString()));
        System.out.println(String.format("%-10s:%s", "EPC", this.epc));
        System.out.println(String.format("%-10s:%d", "AntennaID", this.antennaID));
        System.out.println(String.format("%-10s:%d", "peakRssi", this.peakRssi));
        System.out.println(String.format("%-10s:%d", "SeenCount", this.seenCount));
        System.out.println();
    }

    public void accessCommandOpSpecResultReport() {
        System.out.println(">>>>>> AccessCommandOpSpecResult Report Start");
        for (AccessCommandOpSpecResult opSpecResult : accessCommandOpSpecResultList) {
            System.out.print(">>>> ");
            if (opSpecResult instanceof C1G2ReadOpSpecResult) {
                System.out.println("C1G2ReadOpSpecResult");
                C1G2ReadOpSpecResult readOpSpecResult = (C1G2ReadOpSpecResult) opSpecResult;
                if (readOpSpecResult.getResult().intValue() == C1G2ReadResultType.Success) {
                    System.out.println(readOpSpecResult.getReadData().toString());
                } else {
                    System.out.println(readOpSpecResult.getResult());
                }
            } else if (opSpecResult instanceof C1G2WriteOpSpecResult) {
                System.out.println("C1G2WriteOpSpecResult");
                C1G2WriteOpSpecResult writeOpSpecResult = (C1G2WriteOpSpecResult) opSpecResult;
                if (writeOpSpecResult.getResult().intValue() == C1G2ReadResultType.Success) {
                    System.out.println(writeOpSpecResult.getResult());
                    System.out.println("Written: " + writeOpSpecResult.getNumWordsWritten());
                } else {
                    System.out.println(writeOpSpecResult.getResult());
                }
            } else if (opSpecResult instanceof C1G2LockOpSpecResult) {
                System.out.println("C1G2LockOpSpecResult");
                C1G2LockOpSpecResult lockOpSpecResult = new C1G2LockOpSpecResult();
                System.out.println(lockOpSpecResult.getResult());
            }
        }
        System.out.println(">>>>>> AccessCommandOpSpecResult Report End");
    }

    @Override
    public boolean equals(Object tagData) {
        if (!(tagData instanceof TagData)) {
            return false;
        }
        TagData compareObj = (TagData) tagData;
        return compareObj.epc.equals(this.epc) && compareObj.antennaID == this.antennaID;
    }

    @Override
    public int hashCode() {
        return this.epc.hashCode() + this.antennaID;
    }
}
