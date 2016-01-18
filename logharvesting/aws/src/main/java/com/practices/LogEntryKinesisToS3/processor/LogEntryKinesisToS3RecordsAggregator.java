package com.practices.LogEntryKinesisToS3.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import com.google.common.collect.ImmutableList;
import com.AWSPropertyConfiguration;
import com.amazonaws.services.kinesis.model.Record;
import com.kinesis.aggregator.KinesisRecordsAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ???
 *
 * @author chris_ge
 */
public class LogEntryKinesisToS3RecordsAggregator implements KinesisRecordsAggregator {
    private static final Logger log = LoggerFactory.getLogger(LogEntryKinesisToS3RecordsAggregator.class);

    private final AWSPropertyConfiguration config;
    private final AtomicLong byteCount;
    private final long bytesPerAggregate;
    private final int numMessagesToBuffer;
    private final long millisecondsToBuffer;
    private List<byte[]> buffer;
    private long previousAggregateTimeMillisecond;
    private DataEmitter emitter;
    private String firstSequenceNumber;
    private String lastSequenceNumber;
    private String shardId;

    @Inject
    public LogEntryKinesisToS3RecordsAggregator(AWSPropertyConfiguration config, DataEmitter emitter) {
        this.config = config;
        bytesPerAggregate = config.BUFFER_BYTE_SIZE_LIMIT;
        numMessagesToBuffer = config.BUFFER_RECORD_COUNT_LIMIT;
        millisecondsToBuffer = config.BUFFER_MILLISECONDS_LIMIT;
        this.emitter = emitter;
        // Create a reusable buffer to drain our queue into.
        buffer = new ArrayList<byte[]>(numMessagesToBuffer);
        byteCount = new AtomicLong();
        previousAggregateTimeMillisecond = System.currentTimeMillis();

    }

    public void initialize(String shardId) {
        this.shardId = shardId;
    }

    @Override
    public void emit(List<byte[]> emitItems) {
        List<byte[]> unprocessed = ImmutableList.copyOf(buffer);

        try {
            for (int numTries = 0; numTries < config.RETRY_LIMIT; numTries++) {
                unprocessed = emitter.emit(unprocessed, getS3FileName(firstSequenceNumber, lastSequenceNumber));

                if (unprocessed.isEmpty()) {
                    break;
                }
                try {
                    Thread.sleep(config.BACKOFF_INTERVAL);
                } catch (InterruptedException e) {
                }
            }
            if (!unprocessed.isEmpty()) {
                emitter.fail(unprocessed);
            }

        } catch (IOException e) {
            log.error("Failed to emit data to S3", e);
            emitter.fail(unprocessed);
        } finally {
            buffer.clear();
            byteCount.set(0);
            previousAggregateTimeMillisecond = System.currentTimeMillis();
        }
    }

    @Override
    public void consumeRecord(Record record, int recordSize, String sequenceNumber) {
        if (buffer.isEmpty()) {
            firstSequenceNumber = sequenceNumber;
        }
        lastSequenceNumber = sequenceNumber;
        buffer.add(record.getData().array());
        byteCount.addAndGet(recordSize);
    }

    @Override
    public byte[] fromClass(Record record) {
        return new byte[0];
    }

    @Override
    public boolean shouldFlush() {
        long timelapseMillisecond = System.currentTimeMillis() - previousAggregateTimeMillisecond;
        return (!buffer.isEmpty())
                && ((buffer.size() >= numMessagesToBuffer) || (byteCount.get() >= bytesPerAggregate) || (timelapseMillisecond >= millisecondsToBuffer));

    }

    @Override
    public void shutdown() {
        emitter.shutdown();
    }

    protected String getS3FileName(String firstSeq, String lastSeq) {
        return firstSeq + "-" + lastSeq;
    }

    @Override
    public String getFirstSequenceNumber() {
        return firstSequenceNumber;
    }

    @Override
    public String getLastSequenceNumber() {
        return lastSequenceNumber;
    }

    @Override
    public List<byte[]> getBuffer() {
        return buffer;
    }

    @Override
    public void checkpoint() throws InterruptedException {
    }

}
