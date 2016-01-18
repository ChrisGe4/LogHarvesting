package com.practices.LogEntryKinesisToS3.model;

public enum LogType {
    LOGIN(1),
    PAGE_VIEW(2),
    SERVICE_CALL(3);

    private int value;

    private LogType(int value) {
        this.value = value;
    }

    public static LogType fromValue(int v) {
        if (LOGIN.getValue() == v) {
            return LOGIN;
        } else if (PAGE_VIEW.getValue() == v) {
            return PAGE_VIEW;
        } else {
            return SERVICE_CALL;
        }
    }

    public int getValue() {
        return value;
    }

}
