package com.kinesis.aggregator;

import java.util.List;
import com.amazonaws.services.kinesis.model.Record;

/**
 * ???
 *
 * @author chris_ge
 */
public interface KinesisRecordsAggregator {
    public void initialize(String shardId);

    public void emit(List<byte[]> emitItems);

    public void shutdown();

    public boolean shouldFlush();

    public void consumeRecord(Record record, int recordSize, String sequenceNumber);

    public byte[] fromClass(Record record);

    public String getFirstSequenceNumber();

    public String getLastSequenceNumber();

    public List<byte[]> getBuffer();

    public void checkpoint() throws InterruptedException;

}
