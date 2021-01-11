package com.yjjj.rfid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.enumerations.AccessReportTriggerType;
import org.llrp.ltk.generated.enumerations.AccessSpecState;
import org.llrp.ltk.generated.enumerations.AccessSpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.AirProtocols;
import org.llrp.ltk.generated.enumerations.GetReaderCapabilitiesRequestedData;
import org.llrp.ltk.generated.enumerations.GetReaderConfigRequestedData;
import org.llrp.ltk.generated.enumerations.ROSpecState;
import org.llrp.ltk.generated.enumerations.StatusCode;
import org.llrp.ltk.generated.interfaces.AccessCommandOpSpecResult;
import org.llrp.ltk.generated.interfaces.AirProtocolInventoryCommandSettings;
import org.llrp.ltk.generated.interfaces.AirProtocolTagData;
import org.llrp.ltk.generated.messages.ADD_ACCESSSPEC;
import org.llrp.ltk.generated.messages.ADD_ACCESSSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.ADD_ROSPEC;
import org.llrp.ltk.generated.messages.ADD_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.DELETE_ACCESSSPEC;
import org.llrp.ltk.generated.messages.DELETE_ACCESSSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.DELETE_ROSPEC;
import org.llrp.ltk.generated.messages.DELETE_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.ENABLE_ACCESSSPEC;
import org.llrp.ltk.generated.messages.ENABLE_ACCESSSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES_RESPONSE;
import org.llrp.ltk.generated.messages.GET_READER_CONFIG;
import org.llrp.ltk.generated.messages.GET_READER_CONFIG_RESPONSE;
import org.llrp.ltk.generated.messages.READER_EVENT_NOTIFICATION;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.generated.messages.START_ROSPEC;
import org.llrp.ltk.generated.messages.START_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.parameters.AccessCommand;
import org.llrp.ltk.generated.parameters.AccessReportSpec;
import org.llrp.ltk.generated.parameters.AccessSpec;
import org.llrp.ltk.generated.parameters.AccessSpecStopTrigger;
import org.llrp.ltk.generated.parameters.AntennaConfiguration;
import org.llrp.ltk.generated.parameters.AntennaEvent;
import org.llrp.ltk.generated.parameters.AntennaProperties;
import org.llrp.ltk.generated.parameters.C1G2Read;
import org.llrp.ltk.generated.parameters.C1G2TagSpec;
import org.llrp.ltk.generated.parameters.C1G2TargetTag;
import org.llrp.ltk.generated.parameters.C1G2UHFRFModeTable;
import org.llrp.ltk.generated.parameters.C1G2UHFRFModeTableEntry;
import org.llrp.ltk.generated.parameters.C1G2Write;
import org.llrp.ltk.generated.parameters.ConnectionAttemptEvent;
import org.llrp.ltk.generated.parameters.ConnectionCloseEvent;
import org.llrp.ltk.generated.parameters.EPC_96;
import org.llrp.ltk.generated.parameters.FrequencyInformation;
import org.llrp.ltk.generated.parameters.GeneralDeviceCapabilities;
import org.llrp.ltk.generated.parameters.ROSpec;
import org.llrp.ltk.generated.parameters.ReaderEventNotificationData;
import org.llrp.ltk.generated.parameters.ReaderExceptionEvent;
import org.llrp.ltk.generated.parameters.ReceiveSensitivityTableEntry;
import org.llrp.ltk.generated.parameters.RegulatoryCapabilities;
import org.llrp.ltk.generated.parameters.TagReportData;
import org.llrp.ltk.generated.parameters.TransmitPowerLevelTableEntry;
import org.llrp.ltk.generated.parameters.UHFBandCapabilities;
import org.llrp.ltk.net.LLRPConnection;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.llrp.ltk.net.LLRPConnector;
import org.llrp.ltk.net.LLRPEndpoint;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.BitArray_HEX;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.TwoBitField;
import org.llrp.ltk.types.UnsignedByte;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedLong_DATETIME;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.types.UnsignedShortArray_HEX;

public class RFIDReader implements LLRPEndpoint {
    public interface ReaderEvent {
        void onTagDataUpdate(TagData tagData);
    }

    private int operationTimeout = Const.TIMEOUT_DEFAULT;
    private AccessSpec defaultAccessSpec;
    private ConsolePrint log;
    private LLRPConnection connection;
    private LLRPConnector connector;
    private boolean isConnected = false;
    private String hostname;
    private int port = 5084;
    private List<TagData> tagDatas;
    private ReaderEvent readerEvent;
    private ROSpecParameter roSpecParameter;
    private ROSpec roSpec;

    public RFIDReader(String hostname, ReaderEvent readerEvent) {
        this.log = new ConsolePrint();
        this.hostname = hostname;
        this.connection = new LLRPConnector(this, this.hostname);
        this.connector = ((LLRPConnector) connection);
        this.readerEvent = readerEvent;
        this.defaultAccessSpec = this.bulidDefaultAccessSpec();
    }

    public RFIDReader(String hostname, int port, ReaderEvent readerEvent) {
        this.log = new ConsolePrint();
        this.hostname = hostname;
        this.port = port;
        this.connection = new LLRPConnector(this, this.hostname, this.port);
        this.readerEvent = readerEvent;
    }

    public void changeTimeout(int seconds) {
        this.operationTimeout = seconds * 1000;
    }

    public void enableDebug() {
        this.log.enableDebug();
    }

    public void disableDebug() {
        this.log.disableDebug();
    }

    public void setROSpecs(ROSpecParameter roSpecParameter) {
        this.roSpecParameter = roSpecParameter;
        this.roSpec = this.buildROSpec();
    }

    @Override
    public void errorOccured(String errMessage) {
        this.log.error("Error " + errMessage);
        disconnect();
    }

    @Override
    public void messageReceived(LLRPMessage message) {
        if (message.getTypeNum() == RO_ACCESS_REPORT.TYPENUM) {
            // RoSpec and Access Report.
            RO_ACCESS_REPORT report = (RO_ACCESS_REPORT) message;
            List<TagReportData> tags = report.getTagReportDataList();
            if (tags.size() > 0) {
                for (TagReportData tag : tags) {
                    String epc = ((EPC_96) tag.getEPCParameter()).getEPC().toString();
                    int antennaID = tag.getAntennaID().getAntennaID().intValue();
                    int peakRssi = tag.getPeakRSSI().getPeakRSSI().toInteger();
                    UnsignedLong_DATETIME lastSeenUTC = tag.getLastSeenTimestampUTC().getMicroseconds();
                    int seenCount = tag.getTagSeenCount().getTagCount().toInteger();
                    int channelIndex = tag.getChannelIndex().getChannelIndex().intValue();
                    int accessSpecID = tag.getAccessSpecID().getAccessSpecID().intValue();
                    List<AccessCommandOpSpecResult> accessCommandOpSpecResult = tag.getAccessCommandOpSpecResultList();
                    List<AirProtocolTagData> airProtocolTagData = tag.getAirProtocolTagDataList();
                    TagData currentTag = new TagData(epc, antennaID, peakRssi, lastSeenUTC, seenCount, channelIndex,
                            accessSpecID, accessCommandOpSpecResult, airProtocolTagData);
                    this.readerEvent.onTagDataUpdate(currentTag);
                    int duplicatIndex = tagDatas.indexOf(currentTag);
                    if (duplicatIndex != -1) {
                        this.tagDatas.set(duplicatIndex, currentTag);
                    } else {
                        this.tagDatas.add(currentTag);
                    }
                }
            }
        } else if (message.getTypeNum() == READER_EVENT_NOTIFICATION.TYPENUM) {
            // Reader Event Notification
            READER_EVENT_NOTIFICATION event = (READER_EVENT_NOTIFICATION) message;
            ReaderEventNotificationData eventData = event.getReaderEventNotificationData();
            if (eventData.getConnectionAttemptEvent() != null) {
                ConnectionAttemptEvent connectionAttemptEvent = eventData.getConnectionAttemptEvent();
                this.log.debug(connectionAttemptEvent.getStatus().toString(), "ConnectionAttemptEvent");
            }
            if (eventData.getConnectionCloseEvent() != null) {
                ConnectionCloseEvent connectionCloseEvent = eventData.getConnectionCloseEvent();
                this.log.debug(connectionCloseEvent.getName(), "ConnectionCloseEvent");
            }
            if (eventData.getAntennaEvent() != null) {
                AntennaEvent antennaEvent = eventData.getAntennaEvent();
                this.log.debug(String.format("Antenna %d: %s", antennaEvent.getAntennaID().intValue(),
                        antennaEvent.getEventType().toString()), "AntennaEvent");
            }
            if (eventData.getReaderExceptionEvent() != null) {
                ReaderExceptionEvent exceptionEvent = eventData.getReaderExceptionEvent();
                this.log.error(exceptionEvent.getMessage().toString(), "ReaderExceptionEvent");
            }
        }
    }

    public boolean connect() {
        if (this.isConnected) {
            this.log.warning("Already Connected");
            return this.isConnected;
        }
        try {
            this.log.info(String.format("Connecting to Reader (%s:%d) ...", hostname, port));
            this.connector.connect();
            this.isConnected = true;
            this.log.info(String.format("Reader Connected"));
        } catch (LLRPConnectionAttemptFailedException err) {
            this.log.error("Connect Reader Failed!");
            this.isConnected = false;
            err.printStackTrace();
        }
        return this.isConnected;
    }

    public void addAccessSpec(AccessSpec accessSpec) {
        this.log.debug("AddAccessSpecs");
        ADD_ACCESSSPEC add_ACCESSSPEC = new ADD_ACCESSSPEC();
        add_ACCESSSPEC.setAccessSpec(accessSpec);
        try {
            ADD_ACCESSSPEC_RESPONSE response = (ADD_ACCESSSPEC_RESPONSE) connection.transact(add_ACCESSSPEC,
                    this.operationTimeout);
            if (response != null) {
                System.out.println("\t(" + response.getLLRPStatus().getStatusCode().toString() + ")");
                if (response.getLLRPStatus().getStatusCode().intValue() != StatusCode.M_Success) {
                    this.log.error(response.toXMLString(), "AddAccessSpecsError");
                }
            }
        } catch (TimeoutException | InvalidLLRPMessageException e) {
            e.printStackTrace();
        }

    }

    private void deleteAccessSpecs() {
        this.log.debug("DeleteAccessSpecs");
        DELETE_ACCESSSPEC accessSpecDelete = new DELETE_ACCESSSPEC();
        accessSpecDelete.setAccessSpecID(new UnsignedInteger(0));
        try {
            DELETE_ACCESSSPEC_RESPONSE response = (DELETE_ACCESSSPEC_RESPONSE) connection.transact(accessSpecDelete,
                    this.operationTimeout);
            this.log.debug(response.getLLRPStatus().getStatusCode().toString());
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void enableAccessSpec() {
        this.log.debug("EnableAccessSpecs");
        ENABLE_ACCESSSPEC enable_ACCESSSPEC = new ENABLE_ACCESSSPEC();
        enable_ACCESSSPEC.setAccessSpecID(new UnsignedInteger(Const.ID_ACCESS_SPEC));
        try {
            ENABLE_ACCESSSPEC_RESPONSE response = (ENABLE_ACCESSSPEC_RESPONSE) connection.transact(enable_ACCESSSPEC,
                    this.operationTimeout);
            this.log.debug(response.getLLRPStatus().getStatusCode().toString());
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void addROSpec(ROSpec roSpec) {
        this.log.debug("AddROSpec");
        ADD_ROSPEC roSpecMsg = new ADD_ROSPEC();
        roSpecMsg.setROSpec(roSpec);
        ;
        try {
            ADD_ROSPEC_RESPONSE response = (ADD_ROSPEC_RESPONSE) connection.transact(roSpecMsg, this.operationTimeout);

            // Check if the we successfully added the ROSpec.
            StatusCode status = response.getLLRPStatus().getStatusCode();
            if (status.equals(new StatusCode("M_Success"))) {
                this.log.debug(response.getLLRPStatus().getStatusCode().toString());
            } else {
                this.log.error(response.toXMLString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteROSpecs() {
        this.log.debug("DeleteROSpecs");

        DELETE_ROSPEC rosSpecDelete = new DELETE_ROSPEC();
        rosSpecDelete.setROSpecID(new UnsignedInteger(0));
        try {
            DELETE_ROSPEC_RESPONSE response = (DELETE_ROSPEC_RESPONSE) connection.transact(rosSpecDelete,
                    this.operationTimeout);
            this.log.debug(response.getLLRPStatus().getStatusCode().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableROSpec() {
        this.log.debug("EnableROSpec");
        ENABLE_ROSPEC enable = new ENABLE_ROSPEC();
        enable.setROSpecID(new UnsignedInteger(Const.ID_ROSPEC));

        try {
            ENABLE_ROSPEC_RESPONSE response = (ENABLE_ROSPEC_RESPONSE) connection.transact(enable, operationTimeout);
            this.log.debug(response.getLLRPStatus().getStatusCode().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AccessSpec bulidDefaultAccessSpec() {
        this.log.debug("BulidDefaultAccessSpec");
        try {
            AccessSpec accessSpec = new AccessSpec();
            accessSpec.setCurrentState(new AccessSpecState(AccessSpecState.Disabled));
            accessSpec.setAccessSpecID(new UnsignedInteger(Const.ID_ACCESS_SPEC));
            accessSpec.setAntennaID(new UnsignedShort(0));
            accessSpec.setROSpecID(new UnsignedInteger(Const.ID_ROSPEC));
            accessSpec.setProtocolID(new AirProtocols(AirProtocols.EPCGlobalClass1Gen2));

            // AccessSpecStopTrigger
            AccessSpecStopTrigger specStopTrigger = new AccessSpecStopTrigger();
            specStopTrigger
                    .setAccessSpecStopTrigger(new AccessSpecStopTriggerType(AccessSpecStopTriggerType.Operation_Count));
            // OperationCountValue indicate the number of times this AccessSpec
            // is executed before it is deleted. If set to 0, this is equivalent
            // to no stop trigger defined.
            specStopTrigger.setOperationCountValue(new UnsignedShort(0));
            accessSpec.setAccessSpecStopTrigger(specStopTrigger);

            // AccessCommand
            AccessCommand command = new AccessCommand();
            String targeEPC = "300833b2ddd9014000000011";
            String writeEPC = "3008 33b2 ddd9 0140 0000 0011";
            // ======== TagSpec ========
            // Set C1G2TagSpec of AccessCommand
            C1G2TagSpec tagSpec = new C1G2TagSpec();
            C1G2TargetTag targetTag = new C1G2TargetTag();
            // None Target (all tag)
            // String targetMask = "00000000" + "00000000" + "00000000";
            // Have Target
            String targetMask = "11111111" + "11111111" + "11111111";
            targetTag.setTagMask(new BitArray_HEX(targetMask));
            // set MB=1 and Point=32 bits to match EPC memory
            targetTag.setMB(new TwoBitField("1"));
            targetTag.setPointer(new UnsignedShort(32));
            targetTag.setTagData(new BitArray_HEX(targeEPC));
            targetTag.setMatch(new Bit(1));
            tagSpec.addToC1G2TargetTagList(targetTag);
            command.setAirProtocolTagSpec(tagSpec);
            // ======== C1G2Read (EPC) ========
            // // Set C1G2 OpSpec of AccessCommand
            C1G2Read c1g2ReadEPC = new C1G2Read();
            c1g2ReadEPC.setOpSpecID(new UnsignedShort(Const.ID_OP_SPEC_READ_EPC));
            c1g2ReadEPC.setMB(new TwoBitField("1"));
            // EPC=1;TID=2
            // if WordPointer=0, set WordCount 8(EPC);6(TID)*16bits
            // if WordPointer=2, set WordCount 6 to read EPC memory
            c1g2ReadEPC.setWordPointer(new UnsignedShort(2));
            c1g2ReadEPC.setWordCount(new UnsignedShort(6));
            // Note(Read EPC):return {CRC-PC-EPC}={16 bits-16 bits-96 bits}
            // Note(Read TID):return {TID}={24 bits}
            c1g2ReadEPC.setAccessPassword(new UnsignedInteger(0));
            command.addToAccessCommandOpSpecList(c1g2ReadEPC);
            // ======== C1G2Read (TID) ========
            // // Set C1G2 OpSpec of AccessCommand
            C1G2Read c1g2ReadTID = new C1G2Read();
            c1g2ReadTID.setOpSpecID(new UnsignedShort(Const.ID_OP_SPEC_READ_TID));
            c1g2ReadTID.setMB(new TwoBitField("2"));
            // EPC=1;TID=2
            // if WordPointer=0, set WordCount 8(EPC);6(TID)*16bits
            // if WordPointer=2, set WordCount 6 to read EPC memory
            c1g2ReadTID.setWordPointer(new UnsignedShort(0));
            c1g2ReadTID.setWordCount(new UnsignedShort(6));
            // Note(Read EPC):return {CRC-PC-EPC}={16 bits-16 bits-96 bits}
            // Note(Read TID):return {TID}={24 bits}
            c1g2ReadTID.setAccessPassword(new UnsignedInteger(0));
            command.addToAccessCommandOpSpecList(c1g2ReadTID);
            // // ======== C1G2Lock ========
            // C1G2Lock c1g2Lock =new C1G2Lock();
            // c1g2Lock.setOpSpecID(new
            // UnsignedShort(V.integer.ID_OP_SPEC_LOCK));
            // C1G2LockPayload c1g2LockPayload =new C1G2LockPayload();
            // c1g2LockPayload.setPrivilege(new
            // C1G2LockPrivilege(C1G2LockPrivilege.Read_Write));
            // c1g2LockPayload.setDataField(new
            // C1G2LockDataField(C1G2LockDataField.EPC_Memory));
            // c1g2Lock.addToC1G2LockPayloadList(c1g2LockPayload);
            // c1g2Lock.setAccessPassword(new UnsignedInteger(0));
            // command.addToAccessCommandOpSpecList(c1g2Lock);
            // // ======== C1G2write ========
            C1G2Write c1g2Write = new C1G2Write();
            c1g2Write.setOpSpecID(new UnsignedShort(Const.ID_OP_SPEC_WRITE));
            c1g2Write.setMB(new TwoBitField("1")); // EPC=1;TID=2
            // set WordPointer 2 to write ECP memory
            c1g2Write.setWordPointer(new UnsignedShort(2));
            c1g2Write.setWriteData(new UnsignedShortArray_HEX(writeEPC));
            c1g2Write.setAccessPassword(new UnsignedInteger(0));
            command.addToAccessCommandOpSpecList(c1g2Write);
            accessSpec.setAccessCommand(command);

            // AccessReportSpec
            AccessReportSpec reportSpec = new AccessReportSpec();
            reportSpec.setAccessReportTrigger(
                    new AccessReportTriggerType(AccessReportTriggerType.Whenever_ROReport_Is_Generated));
            accessSpec.setAccessReportSpec(reportSpec);

            return accessSpec;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ROSpec buildROSpec() {
        this.log.debug("BuildROSpec");

        ROSpec roSpec = new ROSpec();
        // ROSpec: Priority must always be set to 0.
        roSpec.setPriority(new UnsignedByte(0));
        // ROSpec: The current state of the ROSpec.
        // When the application is adding ROSpecs, State must be set to Disabled
        roSpec.setCurrentState(new ROSpecState(ROSpecState.Disabled));
        roSpec.setROSpecID(new UnsignedInteger(Const.ID_ROSPEC));

        // Set up the ROBoundarySpec to ROSpec
        // roSpec.setROBoundarySpec(buildROBoundarySpec_Null());
        // roSpec.setROBoundarySpec(buildROBoundarySpec_Periodic());
        roSpec.setROBoundarySpec(this.roSpecParameter.roBoundarySpec());

        // set AISpec to ROSpec
        roSpec.addToSpecParameterList(this.roSpecParameter.antennaSpec());

        // set ROReportSpec to RoSpec
        roSpec.setROReportSpec(this.roSpecParameter.roReportSpec());

        return roSpec;
    }

    private void startROSpec() {
        this.log.debug("StartROSpec");
        START_ROSPEC start = new START_ROSPEC();
        start.setROSpecID(new UnsignedInteger(Const.ID_ROSPEC));
        try {
            START_ROSPEC_RESPONSE response = (START_ROSPEC_RESPONSE) connection.transact(start, operationTimeout);
            this.log.debug(response.getLLRPStatus().getStatusCode().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        this.tagDatas = new ArrayList<TagData>();
        this.deleteAccessSpecs();
        this.deleteROSpecs();
        this.addAccessSpec(this.defaultAccessSpec);
        this.enableAccessSpec();
        this.addROSpec(this.roSpec);
        this.enableROSpec();
        this.startROSpec();
    }

    public void run(AccessSpec accessSpec) {
        this.log.debug("Start Reading");
        this.tagDatas = new ArrayList<TagData>();
        this.deleteAccessSpecs();
        this.deleteROSpecs();
        this.addAccessSpec(accessSpec);
        this.enableAccessSpec();
        this.addROSpec(this.roSpec);
        this.enableROSpec();
        this.startROSpec();
    }

    public void stop() {
        if (this.isConnected) {
            this.log.debug("Stop Reading");
            deleteAccessSpecs();
            deleteROSpecs();
        }
    }

    public boolean disconnect() {
        if (!this.isConnected) {
            this.log.warning("Already Disconnect");
            return this.isConnected;
        }
        this.connector.disconnect();
        this.isConnected = false;
        this.log.info("Reader Disconnected.");
        return this.isConnected;
    }

    /** Show and return the capabilities of Reader */
    public GET_READER_CAPABILITIES_RESPONSE fetchReaderCapabilities() {
        GET_READER_CAPABILITIES getCapabilities = new GET_READER_CAPABILITIES();
        GetReaderCapabilitiesRequestedData requestedData = new GetReaderCapabilitiesRequestedData(
                GetReaderCapabilitiesRequestedData.All);
        getCapabilities.setRequestedData(requestedData);
        GET_READER_CAPABILITIES_RESPONSE response;
        // get.setMessageID(getUniqueMessageID());
        try {
            System.out.println(">>>>>> Fetching Reader Capbilities ...");
            LLRPMessage llrpMessage = connection.transact(getCapabilities, operationTimeout);

            // check whether GET_CAPABILITIES call was successful
            response = (GET_READER_CAPABILITIES_RESPONSE) llrpMessage;
            if (response.getLLRPStatus().getStatusCode().equals(new StatusCode("M_Success"))) {
                // Get the GET_READER_CAPABILITIES_RESPONSE
                System.out.println(String.format(">>>> %s %s", response.getName(), "Report Start"));
                System.out.println(String.format("%-30s: %d", "TypeNum", response.getTypeNum().intValue()));
                System.out.println(String.format("%-30s %s", "AirProtocolLLRPCapabilities",
                        response.getAirProtocolLLRPCapabilities().toString()));
                System.out.println(
                        String.format("%-30s: %s", "LLRPCapabilities", response.getLLRPCapabilities().toString()));
                System.out.println(String.format("%-30s: %d", "CommunicationsStandard",
                        response.getRegulatoryCapabilities().getCommunicationsStandard().intValue()));
                System.out.println(String.format("%-30s: %s", "CustomList", response.getCustomList().toString()));
                System.out.println(String.format("%-30s: %s", "LLRPStatus", response.getLLRPStatus().toString()));
                System.out.println(String.format("%-30s: %s", "Version", response.getVersion().toString()));
                System.out.println(String.format("%-30s: %s", "ResponseType", response.getResponseType()));
                System.out.println(String.format("%-30s: %d", "MessageID", response.getMessageID().intValue()));
                System.out.println(String.format(">>>> %s %s", response.getName(), "Report End"));

                // Get the General Device Capabilities
                GeneralDeviceCapabilities capabilities = response.getGeneralDeviceCapabilities();
                if (capabilities == null) {
                    System.out.println(">>>> General Device Capabilities not available");
                } else {
                    System.out.println(String.format(">>>> %s %s", capabilities.getName(), "Report Start"));
                    System.out.println(String.format("%-40s: %d", "TypeNum", capabilities.getTypeNum().intValue()));
                    System.out.println(
                            String.format("%-40s: %d", "Reader model", capabilities.getModelName().intValue()));
                    System.out.println(String.format("%-40s: %d", "Reader DeviceManufacturerName",
                            capabilities.getDeviceManufacturerName().intValue()));
                    System.out.println(String.format("%-40s: %s", "CanSetAntennaProperties",
                            capabilities.getCanSetAntennaProperties().toString()));
                    System.out.println(String.format("%-40s: %s", "GPIOCapabilities",
                            capabilities.getGPIOCapabilities().toString()));
                    System.out.println(String.format("%-40s: %b", "HasUTCClockCapability",
                            capabilities.getHasUTCClockCapability().toBoolean()));
                    System.out.println(String.format("%-40s: %s", "MaxNumberOfAntennaSupported",
                            capabilities.getMaxNumberOfAntennaSupported().toString()));
                    System.out.println(String.format("%-40s: %s", "ReaderFirmwareVersion",
                            capabilities.getReaderFirmwareVersion().toString()));
                    System.out.println(String.format("%-40s: %s", "PerAntennaAirProtocolList",
                            capabilities.getPerAntennaAirProtocolList().toString()));
                    System.out.println(String.format("%-40s: %s", "PerAntennaReceiveSensitivityRangeList",
                            capabilities.getPerAntennaReceiveSensitivityRangeList().toString()));

                    System.out.println(String.format("%-40s:", "Receive Sensitivity Table"));
                    List<ReceiveSensitivityTableEntry> rSensitivityTableList = capabilities
                            .getReceiveSensitivityTableEntryList();
                    for (ReceiveSensitivityTableEntry entry : rSensitivityTableList) {
                        System.out.println(String.format("Index %3d: %d", entry.getIndex().intValue(),
                                entry.getReceiveSensitivityValue().intValue()));
                    }
                    System.out.println(String.format(">>>> %s %s", capabilities.getName(), "Report End"));
                }
                System.out.println();

                // Get the RegulatoryCapabilities
                RegulatoryCapabilities regulatoryCap = response.getRegulatoryCapabilities();
                if (regulatoryCap != null) {
                    System.out.println(String.format(">>>> %s %s", regulatoryCap.getName(), "Report Start"));
                    System.out.println(String.format("%-30s: %d", "TypeNum", regulatoryCap.getTypeNum().intValue()));
                    System.out.println(String.format("%-30s: %s", "CommunicationsStandard",
                            regulatoryCap.getCommunicationsStandard().toString()));
                    System.out.println(
                            String.format("%-30s: %d", "CountryCode", regulatoryCap.getCountryCode().intValue()));
                    System.out.println(
                            String.format("%-30s: %s", "CustomList", regulatoryCap.getCustomList().toString()));
                    System.out.println(String.format(">>>> %s %s", regulatoryCap.getName(), "Report End"));

                    UHFBandCapabilities bandCap = regulatoryCap.getUHFBandCapabilities();
                    System.out.println(String.format(">>>> %s %s", bandCap.getName(), "Report Start"));
                    System.out.println(String.format("%-30s: %d", "TypeNum", bandCap.getTypeNum().intValue()));
                    System.out.println(String.format("%-30s:", "Transmit Power Level Table"));
                    List<TransmitPowerLevelTableEntry> txPowerTableList = bandCap.getTransmitPowerLevelTableEntryList();
                    for (TransmitPowerLevelTableEntry entry : txPowerTableList) {
                        // LLRP sends power in (dBm * 100)
                        System.out.println(String.format("Index %3d: %d", entry.getIndex().intValue(),
                                entry.getTransmitPowerValue().intValue() / 100.0));
                    }

                    if (bandCap.getAirProtocolUHFRFModeTableList().size() > 0) {
                        // Get the first C1G2 UHF RF Mode Table
                        C1G2UHFRFModeTable uhfRFModeTable = (C1G2UHFRFModeTable) bandCap
                                .getAirProtocolUHFRFModeTableList().get(0);

                        System.out.println(">>>>" + uhfRFModeTable.getName());
                        System.out
                                .println(String.format("%-30s: %d", "TypeNum", uhfRFModeTable.getTypeNum().intValue()));

                        System.out.println(String.format("%-30s:", "C1G2 UHF RF Mode Table"));
                        List<C1G2UHFRFModeTableEntry> UHFRFModeTableList = uhfRFModeTable
                                .getC1G2UHFRFModeTableEntryList();

                        for (C1G2UHFRFModeTableEntry entry : UHFRFModeTableList) {
                            System.out.println(
                                    String.format("%-30s: %d", "ModeIdentifier", entry.getModeIdentifier().intValue()));
                            System.out.println(String.format("%-30s: %s", "DRValue", entry.getDRValue().toString()));
                            System.out.println(String.format("%-30s: %d", "BDRValue", entry.getBDRValue().intValue()));
                            System.out.println(String.format("%-30s: %s", "MValue", entry.getMValue().toString()));
                            System.out.println(String.format("%-30s: %d", "PIEValue", entry.getPIEValue().intValue()));
                            System.out.println(String.format("%-30s: %s", "ForwardLinkModulation",
                                    entry.getForwardLinkModulation().toString()));
                            System.out.println(
                                    String.format("%-30s: %d", "MinTariValue", entry.getMinTariValue().intValue()));
                            System.out.println(
                                    String.format("%-30s: %d", "MaxTariValue", entry.getMaxTariValue().intValue()));
                            System.out.println(
                                    String.format("%-30s: %d", "StepTariValue", entry.getStepTariValue().intValue()));
                            System.out.println(String.format("%-30s: %s", "SpectralMaskIndicator",
                                    entry.getSpectralMaskIndicator().toString()));
                            System.out.println(String.format("%-30s: %s", "EPCHAGTCConformance",
                                    entry.getEPCHAGTCConformance().toString()));
                            System.out.println();
                        }
                    }

                    // Get the FrequencyInformation
                    FrequencyInformation frequencyInfo = bandCap.getFrequencyInformation();
                    System.out.println(">>>>" + frequencyInfo.getName());
                    System.out.println(String.format("%-30s: %d", "TypeNum", frequencyInfo.getTypeNum().intValue()));
                    System.out.println(String.format("%-30s: %s", "Hopping", frequencyInfo.getHopping().toString()));
                    System.out.println(String.format("%-30s: %s", "HopTableList",
                            frequencyInfo.getFrequencyHopTableList().toString()));
                    System.out.println(String.format("%-30s: %s", "FixedFrequencyTable",
                            frequencyInfo.getFixedFrequencyTable().toString()));

                    System.out.println(String.format(">>>> %s %s", bandCap.getName(), "Report End"));
                } else {
                    this.log.error(llrpMessage.toXMLString(), "fetchReaderCapabilitiesError");
                }
            }
        } catch (InvalidLLRPMessageException e) {
            this.log.error(e.toString(), "fetchReaderCapabilitiesError.InvalidLLRPMessageException");
            response = null;
        } catch (TimeoutException e) {
            this.log.error(e.toString(), "fetchReaderCapabilitiesError.TimeoutException");
            response = null;
        }
        return response;
    }

    /** Show and return configuration of Reader */
    public GET_READER_CONFIG_RESPONSE fetchReaderConfiguration() {
        LLRPMessage llrpMessage;
        GET_READER_CONFIG getConfig = new GET_READER_CONFIG();
        // set the request data
        GetReaderConfigRequestedData requestedData = new GetReaderConfigRequestedData(GetReaderConfigRequestedData.All);
        getConfig.setAntennaID(new UnsignedShort(0));
        getConfig.setGPIPortNum(new UnsignedShort(0));
        getConfig.setGPOPortNum(new UnsignedShort(0));
        getConfig.setRequestedData(requestedData);
        GET_READER_CONFIG_RESPONSE response;
        try {
            System.out.println(">>>>>> fetch Reader Configuration ...");
            llrpMessage = connection.transact(getConfig, operationTimeout);
            response = (GET_READER_CONFIG_RESPONSE) llrpMessage;
            if (response.getLLRPStatus().getStatusCode().equals(new StatusCode("M_Success"))) {
                System.out.println(String.format(">>>> %s %s", response.getName(), "Report Start"));
                System.out.println(String.format("%-30s: %d", "TypeNum", response.getTypeNum().intValue()));
                System.out.println(response.toString());

                // Get the Identification
                // The identifier could be the Reader MAC address or EPC.
                System.out.println(response.getIdentification().toString());
                // String readerIdentification =
                // response.getIdentification().getReaderID().toString().substring(0, 6)
                // + response.getIdentification().getReaderID().toString().substring(10);

                // Get the Antenna Properties
                List<AntennaProperties> antennaPropList = response.getAntennaPropertiesList();
                System.out.println(String.format("%-30s:", "Antenna Properties Table"));
                for (AntennaProperties property : antennaPropList) {

                    System.out.println(String.format("%-20s: %d", "AntennaID", property.getAntennaID().intValue()));
                    System.out.println(
                            String.format("%-20s: %b", "AntennaConnected", property.getAntennaConnected().toBoolean()));
                    // The antenna gain(in dBI*100) is the composite gain and
                    // includes the loss of the associated cable from the Reader
                    // to the antenna.
                    System.out.println(
                            String.format("%-20s: %d", "AntennaGain", property.getAntennaGain().intValue() / 100.0));
                    System.out.println();
                }
                // Get the Antenna Configuration
                // It specifies the default values.
                System.out.println(String.format("%-30s:", "Antenna Configuration Table"));
                for (AntennaConfiguration configuration : response.getAntennaConfigurationList()) {
                    System.out
                            .println(String.format("%-20s: %d", "AntennaID", configuration.getAntennaID().intValue()));
                    System.out.println(
                            String.format("%-20s: %d", "RFReceiver", configuration.getRFReceiver().toString()));
                    System.out.println(
                            String.format("%-20s: %d", "RFTransmitter", configuration.getRFTransmitter().toString()));

                    System.out.println(String.format("%-40s:", "AirProtocolInventoryCommandSettings Table"));
                    for (AirProtocolInventoryCommandSettings inventoryComSet : configuration
                            .getAirProtocolInventoryCommandSettingsList()) {
                        System.out.println(String.format("%-20s: %d", "RFTransmitter", inventoryComSet.toString()));
                    }
                }

                System.out.println(String.format("%-20s:", "Other"));
                System.out.println(response.getROReportSpec());
                System.out.println(response.getReaderEventNotificationSpec());
                System.out.println(response.getAccessReportSpec());
                System.out.println(response.getLLRPConfigurationStateValue());
                System.out.println(response.getKeepaliveSpec());
                System.out.println(response.getGPIPortCurrentStateList());
                System.out.println(response.getGPOWriteDataList());
                System.out.println(response.getEventsAndReports());

            } else {
                this.log.error("fetchReaderConfigurationError");
            }

        } catch (TimeoutException e) {
            e.printStackTrace();
            response = null;
        }
        return response;
    }

}
