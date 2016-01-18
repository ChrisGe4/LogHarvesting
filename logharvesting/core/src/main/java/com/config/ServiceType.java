package com.config;

/**
 * @author chris_ge
 */
public enum ServiceType {
    PROJECT("project"),
    S3("s3"),
    KINESIS("kinesis");
    private String type;

    ServiceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
