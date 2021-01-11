package com.yjjj.rfid;

public class ConsolePrint {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private String PREFIX = ">>>>>> ";

    private Boolean isDebug = false;

    public void enableDebug() {
        this.isDebug = true;
    }

    public void disableDebug() {
        this.isDebug = false;
    }

    public void info(String infoMessage) {
        System.out.print(this.PREFIX);
        System.out.println(infoMessage);
        System.out.println();
    }

    public void debug(String debugMessage) {
        if (!this.isDebug) {
            return;
        }
        System.out.print(this.PREFIX);
        System.out.println(debugMessage);
        System.out.println();
    }

    public void debug(String debugMessage, String title) {
        if (!this.isDebug) {
            return;
        }
        System.out.println(this.PREFIX + title);
        System.out.println(debugMessage);
        System.out.println();
    }

    public void warning(String warnMessage) {
        if (!this.isDebug) {
            return;
        }
        System.out.print(ANSI_YELLOW + this.PREFIX);
        System.out.println(warnMessage);
        System.out.println(ANSI_RESET);

    }

    public void warning(String warnMessage, String title) {
        if (!this.isDebug) {
            return;
        }
        System.out.println(ANSI_YELLOW + this.PREFIX + title);
        System.out.println(warnMessage);
        System.out.println();
    }

    public void error(String errorMessage) {
        System.out.print(ANSI_RED + this.PREFIX);
        System.out.println(errorMessage);
        System.out.println(ANSI_RESET);
    }

    public void error(String errorMessage, String title) {
        System.out.println(ANSI_RED + this.PREFIX + title);
        System.out.println(errorMessage);
        System.out.println();
    }
}
