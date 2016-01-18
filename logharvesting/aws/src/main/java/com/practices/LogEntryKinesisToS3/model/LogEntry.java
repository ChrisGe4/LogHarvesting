package com.practices.LogEntryKinesisToS3.model;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LogEntry {
    private final static ObjectMapper JSON = new ObjectMapper();

    /*
    * Consider a producer that experiences a network-related timeout after it makes a call to PutRecord,
    * but before it can receive an acknowledgement from Amazon Kinesis. The producer cannot be sure if the record was delivered to Amazon Kinesis.
    * Assuming that every record is important to the application, the producer would have been written to retry the call with the same data.
    * If both PutRecord calls on that same data were successfully committed to Amazon Kinesis, then there will be two Amazon Kinesis records.
    * Although the two records have identical data, they also have unique sequence numbers. Applications that need strict guarantees should
    * embed a primary key within the record to remove duplicates later when processing.
    * Note that the number of duplicates due to producer retries is usually low compared to the number of duplicates due to consumer retries.
    */
    private String primaryKey;
    private long id;
    private LogType type;
    private String host;
    private long timestamp;

    public LogEntry(String primaryKey, long id, LogType type, String host,
                    long timestamp) {
        this.primaryKey = primaryKey;
        this.id = id;
        this.type = type;
        this.host = host;
        this.timestamp = timestamp;
    }

    public LogEntry() {

    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "primaryKey='" + primaryKey + '\'' +
                ", id=" + id +
                ", type=" + type +
                ", host='" + host + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
