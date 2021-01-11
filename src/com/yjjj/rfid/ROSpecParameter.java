package com.yjjj.rfid;

import org.llrp.ltk.generated.enumerations.AISpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.AirProtocols;
import org.llrp.ltk.generated.enumerations.ROReportTriggerType;
import org.llrp.ltk.generated.enumerations.ROSpecStartTriggerType;
import org.llrp.ltk.generated.enumerations.ROSpecStopTriggerType;
import org.llrp.ltk.generated.parameters.AISpec;
import org.llrp.ltk.generated.parameters.AISpecStopTrigger;
import org.llrp.ltk.generated.parameters.AntennaConfiguration;
import org.llrp.ltk.generated.parameters.C1G2EPCMemorySelector;
import org.llrp.ltk.generated.parameters.C1G2InventoryCommand;
import org.llrp.ltk.generated.parameters.C1G2RFControl;
import org.llrp.ltk.generated.parameters.C1G2SingulationControl;
import org.llrp.ltk.generated.parameters.InventoryParameterSpec;
import org.llrp.ltk.generated.parameters.PeriodicTriggerValue;
import org.llrp.ltk.generated.parameters.RFReceiver;
import org.llrp.ltk.generated.parameters.RFTransmitter;
import org.llrp.ltk.generated.parameters.ROBoundarySpec;
import org.llrp.ltk.generated.parameters.ROReportSpec;
import org.llrp.ltk.generated.parameters.ROSpecStartTrigger;
import org.llrp.ltk.generated.parameters.ROSpecStopTrigger;
import org.llrp.ltk.generated.parameters.TagReportContentSelector;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.TwoBitField;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.types.UnsignedShortArray;

public class ROSpecParameter {
    private int duration;
    private int indexRFMode = 2;
    private int txPower = Const.TX_POWER_IPJ_R420_30dBm;
    private int antennaID = Const.ANTENNA_PORT_ID_ALL;
    private boolean isPeriodic = false;
    private boolean isNull = false;
    UnsignedInteger startDuration = new UnsignedInteger(1000);
    UnsignedInteger stopDuration = new UnsignedInteger(1000);

    public ROSpecParameter(int duration) {
        if (duration > 0) {
            this.duration = duration;
        } else {
            this.isNull = true;
        }
    }

    public ROSpecParameter(UnsignedInteger startDuration, UnsignedInteger stopDuration) {
        // Milliseconds
        this.startDuration = startDuration;
        this.stopDuration = stopDuration;
        this.isPeriodic = true;
    }

    public ROSpecParameter(int duration, int txPower) {
        if (duration > 0) {
            this.duration = duration;
        } else {
            this.isNull = true;
        }
        this.txPower = txPower;
    }

    public ROSpecParameter(UnsignedInteger startDuration, UnsignedInteger stopDuration, int txPower) {
        // Milliseconds
        this.startDuration = startDuration;
        this.stopDuration = stopDuration;
        this.txPower = txPower;
        this.isPeriodic = true;
    }

    public ROBoundarySpec roBoundarySpec() {
        if (!isNull) {
            if (this.isPeriodic) {
                return this.buildROBoundarySpecWithPeriodic();
            } else {
                return this.buildROBoundarySpecWithDuration();
            }
        } else {
            return this.buildROBoundarySpecWithNull();
        }

    }

    public ROReportSpec roReportSpec() {
        // Specify what type of tag reports
        // we want to receive and when we want to receive them.
        ROReportSpec roReportSpec = new ROReportSpec();
        // Receive a report every time a tag is read.
        roReportSpec.setROReportTrigger(new ROReportTriggerType(ROReportTriggerType.Upon_N_Tags_Or_End_Of_ROSpec));
        // Set the number of TagReportData parameters
        // present in a report before the report trigger fires
        roReportSpec.setN(new UnsignedShort(1));
        TagReportContentSelector reportContent = new TagReportContentSelector();
        // Select which fields we want in the report.
        reportContent.setEnableAccessSpecID(new Bit(1));
        reportContent.setEnableAntennaID(new Bit(1));
        reportContent.setEnableChannelIndex(new Bit(1));
        reportContent.setEnableFirstSeenTimestamp(new Bit(0));
        reportContent.setEnableInventoryParameterSpecID(new Bit(0));
        reportContent.setEnableLastSeenTimestamp(new Bit(1));
        reportContent.setEnablePeakRSSI(new Bit(1));
        reportContent.setEnableROSpecID(new Bit(0));
        reportContent.setEnableSpecIndex(new Bit(0));
        reportContent.setEnableTagSeenCount(new Bit(1));

        C1G2EPCMemorySelector CMS = new C1G2EPCMemorySelector();
        CMS.setEnableCRC(new Bit(1));
        CMS.setEnablePCBits(new Bit(1));

        reportContent.addToAirProtocolEPCMemorySelectorList(CMS);

        roReportSpec.setTagReportContentSelector(reportContent);

        return roReportSpec;
    }

    public AISpec antennaSpec() {
        // Build an Antenna Inventory Spec (AISpec).
        AISpec aispec = new AISpec();

        // Set the AI stop trigger to null.
        // This means that the AIspec will run until the ROSpec stops.
        AISpecStopTrigger aiStopTrigger = new AISpecStopTrigger();
        aiStopTrigger.setAISpecStopTriggerType(new AISpecStopTriggerType(AISpecStopTriggerType.Null));
        aiStopTrigger.setDurationTrigger(new UnsignedInteger(0));

        // Set Trigger Example
        // aiStopTrigger.setAISpecStopTriggerType(new AISpecStopTriggerType(
        // AISpecStopTriggerType.Tag_Observation));
        // TagObservationTrigger tagObservationTrigger = new
        // TagObservationTrigger();
        // tagObservationTrigger.setNumberOfTags(new UnsignedShort(1));
        // tagObservationTrigger.setTimeout(new UnsignedInteger(0));
        // tagObservationTrigger.setT(new UnsignedShort(0));
        // tagObservationTrigger.setNumberOfAttempts(new UnsignedShort(2000));
        // aiStopTrigger.setTagObservationTrigger(tagObservationTrigger);

        aispec.setAISpecStopTrigger(aiStopTrigger);

        // Select which antenna ports we want to use.
        // e.g Set this property to 0 means all antenna ports.
        // e.g Set this property to 1 means first antenna port.
        UnsignedShortArray antennaIDs = new UnsignedShortArray();
        antennaIDs.add(new UnsignedShort(this.antennaID));
        aispec.setAntennaIDs(antennaIDs);

        // Tell the reader that we're reading Gen2 tags.
        InventoryParameterSpec inventoryParam = new InventoryParameterSpec();
        inventoryParam.setProtocolID(new AirProtocols(AirProtocols.EPCGlobalClass1Gen2));
        inventoryParam.setInventoryParameterSpecID(new UnsignedShort(Const.ID_INVENTORY_PARAMETER_SPEC));

        AntennaConfiguration atC = new AntennaConfiguration();
        atC.setAntennaID(new UnsignedShort(this.antennaID));

        RFTransmitter RFT = new RFTransmitter();
        RFT.setTransmitPower(new UnsignedShort(this.txPower));
        RFT.setHopTableID(new UnsignedShort(1));
        // ChannelIndex is the one-based channel index in the
        // FixedFrequencyTable to use during transmission
        RFT.setChannelIndex(new UnsignedShort(0));
        atC.setRFTransmitter(RFT);

        RFReceiver RFR = new RFReceiver();
        RFR.setReceiverSensitivity(new UnsignedShort(1));
        atC.setRFReceiver(RFR);

        C1G2InventoryCommand CGIC = new C1G2InventoryCommand();
        CGIC.setTagInventoryStateAware(new Bit(0));
        // Set a “null filter” to delete existing filters on a Reader.
        CGIC.setC1G2FilterList(null);

        // Set the C1G2RF Control Parameter which carries the settings relevant
        // to RF forward and reverse link control in the C1G2 air protocol.
        C1G2RFControl CRF = new C1G2RFControl();
        // Set the ModeIndex:This is an index into the UHFC1G2RFModeTable
        CRF.setModeIndex(new UnsignedShort(indexRFMode));
        // Set the Value of Tari to use for this mode specified in nsec.
        // If the selected mode has a range, and the Tari is set to zero, the
        // Reader implementation picks up any Tari value within the range.
        CRF.setTari(new UnsignedShort(0));
        CGIC.setC1G2RFControl(CRF);

        // Set the C1G2SingulationControl Parameter provides controls
        // particular to the singulation (Identifying an individual Tag in a
        // multiple-Tag environment) process in the C1G2 air protocol.
        C1G2SingulationControl CGS = new C1G2SingulationControl();
        CGS.setSession(new TwoBitField("2")); // set "2" means 10 in bits
        CGS.setTagPopulation(new UnsignedShort(32));
        CGS.setTagTransitTime(new UnsignedInteger(0));
        CGIC.setC1G2SingulationControl(CGS);

        atC.addToAirProtocolInventoryCommandSettingsList(CGIC);
        inventoryParam.addToAntennaConfigurationList(atC);
        aispec.addToInventoryParameterSpecList(inventoryParam);

        return aispec;
    }

    private ROBoundarySpec buildROBoundarySpecWithNull() {
        // Set up the ROBoundarySpec
        // This defines the start and stop triggers.
        ROBoundarySpec roBoundarySpec = new ROBoundarySpec();

        // Set the start trigger to null.
        // This means the ROSpec will start as soon as it is enabled.
        ROSpecStartTriggerType startTriggerType = new ROSpecStartTriggerType(ROSpecStartTriggerType.Null);
        ROSpecStartTrigger startTrig = new ROSpecStartTrigger();
        startTrig.setROSpecStartTriggerType(startTriggerType);
        roBoundarySpec.setROSpecStartTrigger(startTrig);

        // Set the stop trigger is null. This means the ROSpec
        // will keep running until an STOP_ROSPEC message is sent.
        ROSpecStopTriggerType stopTriggerType = new ROSpecStopTriggerType(ROSpecStopTriggerType.Null);
        ROSpecStopTrigger stopTrig = new ROSpecStopTrigger();
        stopTrig.setDurationTriggerValue(new UnsignedInteger(0));
        stopTrig.setROSpecStopTriggerType(stopTriggerType);
        roBoundarySpec.setROSpecStopTrigger(stopTrig);

        return roBoundarySpec;
    }

    private ROBoundarySpec buildROBoundarySpecWithDuration() {
        // Set up the ROBoundarySpec
        // This defines the start and stop triggers.
        ROBoundarySpec roBoundarySpec = new ROBoundarySpec();

        ROSpecStartTriggerType startTriggerType = new ROSpecStartTriggerType(ROSpecStartTriggerType.Null);

        // Set the start trigger to null.
        // This means the ROSpec will start as soon as it is enabled.
        ROSpecStartTrigger startTrig = new ROSpecStartTrigger();
        startTrig.setROSpecStartTriggerType(startTriggerType);
        roBoundarySpec.setROSpecStartTrigger(startTrig);

        // Set the stop trigger is null. This means the ROSpec
        // will keep running until an STOP_ROSPEC message is sent.
        ROSpecStopTriggerType stopTriggerType = new ROSpecStopTriggerType(ROSpecStopTriggerType.Duration);
        ROSpecStopTrigger stopTrigger = new ROSpecStopTrigger();
        stopTrigger.setDurationTriggerValue(new UnsignedInteger(duration));
        stopTrigger.setROSpecStopTriggerType(stopTriggerType);
        roBoundarySpec.setROSpecStopTrigger(stopTrigger);

        return roBoundarySpec;
    }

    private ROBoundarySpec buildROBoundarySpecWithPeriodic() {
        // Set up the ROBoundarySpec
        // This defines the start and stop triggers.
        ROBoundarySpec roBoundarySpec = new ROBoundarySpec();

        // Set the start trigger to Periodic.
        // This means the ROSpec will start every period time.
        // The first start time is determined as
        // (time of message receipt + offset),if UTC time is not specified
        // , or (UTC time + offset)
        // Subsequent start times = first start time + k * period (k is integer)
        ROSpecStartTriggerType startTriggerType = new ROSpecStartTriggerType(ROSpecStartTriggerType.Periodic);
        ROSpecStartTrigger startTrigger = new ROSpecStartTrigger();
        // set the periodic time of ROSpecStartTrigger
        PeriodicTriggerValue periodicValue = new PeriodicTriggerValue();
        periodicValue.setPeriod(this.startDuration);
        periodicValue.setOffset(new UnsignedInteger(0));
        periodicValue.setUTCTimestamp(null);
        startTrigger.setPeriodicTriggerValue(periodicValue);
        startTrigger.setROSpecStartTriggerType(startTriggerType);
        roBoundarySpec.setROSpecStartTrigger(startTrigger);

        // Set the stop trigger is Duration.
        // This means the ROSpec stop after running the Duration Time.
        ROSpecStopTriggerType stopTriggerType = new ROSpecStopTriggerType(ROSpecStopTriggerType.Null);
        // Set the stop Duration time of ROSpecStopTrigger
        ROSpecStopTrigger stopTrig = new ROSpecStopTrigger();
        stopTrig.setDurationTriggerValue(this.stopDuration);

        stopTrig.setROSpecStopTriggerType(stopTriggerType);
        roBoundarySpec.setROSpecStopTrigger(stopTrig);

        return roBoundarySpec;
    }
}
