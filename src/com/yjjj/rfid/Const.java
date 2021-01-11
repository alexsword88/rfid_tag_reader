package com.yjjj.rfid;

public class Const {
    /** Impinj R1000 Transmit power index as power is 30.0 dBm */
    public static final int TX_POWER_IPJ_R1000_30dBm = 61;
    /** Impinj R1000 Transmit power index as power is 27.0 dBm */
    public static final int TX_POWER_IPJ_R1000_27dBm = 49;
    /** Impinj R1000 Transmit power index as power is 24.0 dBm */
    public static final int TX_POWER_IPJ_R1000_24dBm = 37;
    /** Impinj R1000 Transmit power index as power is 21.0 dBm */
    public static final int TX_POWER_IPJ_R1000_21dBm = 25;
    /** Impinj R1000 Transmit power index as power is 18.0 dBm */
    public static final int TX_POWER_IPJ_R1000_18dBm = 13;
    /** Impinj R1000 Transmit power index as power is 15.0 dBm */
    public static final int TX_POWER_IPJ_R1000_15dBm = 1;

    /** 3M Compact 1000 Transmit power index as power is 30.0 dBm */
    public static final int TX_POWER_3M_1000_30dBm = 200;
    /** 3M Compact 1000 Transmit power index as power is 27.0 dBm */
    public static final int TX_POWER_3M_1000_27dBm = 180;
    /** 3M Compact 1000 Transmit power index as power is 24.0 dBm */
    public static final int TX_POWER_3M_1000_24dBm = 140;
    /** 3M Compact 1000 Transmit power index as power is 21.0 dBm */
    public static final int TX_POWER_3M_1000_21dBm = 110;
    /** 3M Compact 1000 Transmit power index as power is 18.0 dBm */
    public static final int TX_POWER_3M_1000_18dBm = 80;
    /** 3M Compact 1000 Transmit power index as power is 15.0 dBm */
    public static final int TX_POWER_3M_1000_15dBm = 50;
    /** 3M Compact 1000 Transmit power index as power is 12.0 dBm */
    public static final int TX_POWER_3M_1000_12dBm = 20;
    /** 3M Compact 1000 Transmit power index as power is 10.0 dBm */
    public static final int TX_POWER_3M_1000_10dBm = 0;

    /** Impinj R420 Transmit power index as power is 32.5 dBm */
    public static final int TX_POWER_IPJ_R420_32dot5dBm = 91;
    /** Impinj R420 Transmit power index as power is 30.0 dBm */
    public static final int TX_POWER_IPJ_R420_30dBm = 81;
    /** Impinj R420 Transmit power index as power is 27.0 dBm */
    public static final int TX_POWER_IPJ_R420_27dBm = 69;
    /** Impinj R420 Transmit power index as power is 24.0 dBm */
    public static final int TX_POWER_IPJ_R420_24dBm = 57;
    /** Impinj R420 Transmit power index as power is 21.0 dBm */
    public static final int TX_POWER_IPJ_R420_21dBm = 45;
    /** Impinj R420 Transmit power index as power is 18.0 dBm */
    public static final int TX_POWER_IPJ_R420_18dBm = 33;
    /** Impinj R420 Transmit power index as power is 15.0 dBm */
    public static final int TX_POWER_IPJ_R420_15dBm = 21;
    /** Impinj R420 Transmit power index as power is 12.0 dBm */
    public static final int TX_POWER_IPJ_R420_12dBm = 9;
    /** Impinj R420 Transmit power index as power is 10.0 dBm */
    public static final int TX_POWER_IPJ_R420_10dBm = 1;

    public static final int ANTENNA_MAX_PORT_IPJ_R420 = 4;

    /** Select all reader antenna ports to use. */
    public static final int ANTENNA_PORT_ID_ALL = 0;
    /** Select reader antenna port 1 to use. */
    public static final int ANTENNA_PORT_ID_1 = 1;
    /** Select reader antenna port 2 to use. */
    public static final int ANTENNA_PORT_ID_2 = 2;
    /** Select reader antenna port 3 to use. */
    public static final int ANTENNA_PORT_ID_3 = 3;
    /** Select reader antenna port 4 to use. */
    public static final int ANTENNA_PORT_ID_4 = 4;

    // Default timeout milliseconds
    public static final int TIMEOUT_DEFAULT = 10000;

    // Spec ID
    public static final int ID_ROSPEC = 1;
    public static final int ID_INVENTORY_PARAMETER_SPEC = 9;
    public static final int ID_ACCESS_SPEC = 2;
    public static final int ID_OP_SPEC_READ_EPC = 3;
    public static final int ID_OP_SPEC_READ_TID = 4;
    public static final int ID_OP_SPEC_WRITE = 5;
    public static final int ID_OP_SPEC_LOCK = 6;
}
