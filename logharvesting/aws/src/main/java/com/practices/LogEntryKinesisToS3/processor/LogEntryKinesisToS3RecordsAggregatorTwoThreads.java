package com.practices.LogEntryKinesisToS3.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
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
public class LogEntryKinesisToS3RecordsAggregatorTwoThreads implements KinesisRecordsAggregator, Runnable {
    private static final Logger log = LoggerFactory.getLogger(LogEntryKinesisToS3RecordsAggregatorTwoThreads.class);

    //private  int MAX_RECORDS_IN_MEMORY=0;

    private final AWSPropertyConfiguration config;
    private final AtomicLong byteCount;
    private final long bytesPerAggregate;
    private final int numMessagesToBuffer;
    private final long millisecondsToBuffer;
    private final ReentrantLock lock = new ReentrantLock();
    Condition dataProcessed = lock.newCondition();
    private BlockingQueue<byte[]> rawData;
    private ScheduledExecutorService aggregatorService;
    private List<byte[]> buffer;
    private long previousAggregateTimeMillisecond;
    private DataEmitter emitter;
    private int initDelay;
    private int delay;
    private String shardId;
    private String firstSequenceNumber;
    private String lastSequenceNumber;

    @Inject
    public LogEntryKinesisToS3RecordsAggregatorTwoThreads(AWSPropertyConfiguration config, DataEmitter emitter) {
        this.config = config;
        initDelay = config.AGGREGATOR_THREAD_INIT_DELAY;
        delay = config.AGGREGATOR_THREAD_DELAY;
        bytesPerAggregate = config.BUFFER_BYTE_SIZE_LIMIT;
        numMessagesToBuffer = config.BUFFER_RECORD_COUNT_LIMIT;
        millisecondsToBuffer = config.BUFFER_MILLISECONDS_LIMIT;

        rawData = new ArrayBlockingQueue<byte[]>(numMessagesToBuffer);
        this.emitter = emitter;
        // Create a reusable buffer to drain our queue into.
        buffer = new ArrayList<byte[]>(numMessagesToBuffer);
        byteCount = new AtomicLong();
        previousAggregateTimeMillisecond = System.currentTimeMillis();

    }

    public void initialize(String shardId) {
        this.shardId = shardId;
        aggregatorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        });
        log.info("Starting queue consumer for shard: " + shardId);

       /* aggregatorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                if (shouldFlush()) aggregate();

            }
        }, initDelay, delay, TimeUnit.MILLISECONDS);*/
        aggregatorService.scheduleWithFixedDelay(this, initDelay, delay, TimeUnit.MILLISECONDS);

    }

    @Override
    public void shutdown() {
        aggregatorService.shutdown();
        try {
            // Wait for at most 30 seconds for the executor service's tasks to complete
            if (!aggregatorService.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Failed to properly shut down interval thread pool for aggregation. Some log entries may not have been aggregated and written to S3.");
            }
        } catch (InterruptedException e) {

            aggregatorService.shutdownNow();
            throw new RuntimeException("Couldn't successfully process data within the max wait time. Skip checkpointing.", e);
        }

        log.info("Queue consumer terminated for shard: " + shardId);

    }

    @Override
    public boolean shouldFlush() {
        long timelapseMillisecond = System.currentTimeMillis() - previousAggregateTimeMillisecond;

        return ((rawData.size() >= numMessagesToBuffer) || (byteCount.get() >= bytesPerAggregate) || (timelapseMillisecond >= millisecondsToBuffer));
    }

    @Override
    public void run() {
        if (shouldFlush()) aggregate();
    }

    private void aggregate() {

        try {
            int drained = 0;

            // Block while waiting for data
            // rawData.poll(maxQueueWaitTimeMs, TimeUnit.MILLISECONDS);
            buffer.add(rawData.take());
            //System.out.println("log = " + first.getId());

            lock.lock();
            drained++;
            // Drain as much of the queue as we can.
            drained += rawData.drainTo(buffer);
            emit(buffer);
            log.info("Consumed " + drained + " records");

            buffer.clear();
            byteCount.set(0);
            previousAggregateTimeMillisecond = System.currentTimeMillis();
            dataProcessed.signalAll();
        } catch (InterruptedException e) {
            log.error("Failed to emit data to S3, thread has been interrupted", e);

        } finally {
            lock.unlock();
        }

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
        }
    }

    @Override
    public void consumeRecord(Record record, int recordSize, String sequenceNumber) {
        if (buffer.isEmpty()) {
            firstSequenceNumber = sequenceNumber;
        }
        lastSequenceNumber = sequenceNumber;
        try {
            rawData.put(record.getData().array());
        } catch (InterruptedException e) {
            log.error("Interrupted while adding a record to the queue", e);
            Thread.currentThread().interrupt();
        }
        byteCount.addAndGet(recordSize);
    }

    private void waitForAggregation() {
        try {
            lock.lock();
            while (!buffer.isEmpty()) {
                dataProcessed.await();
            }
        } catch (InterruptedException e) {
            log.error(e.toString());
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }

        /*synchronized (rawData) {
            while (!rawData.isEmpty()) {
                dataProcessed.await();
            }
        }*/
    }

    public int getMaxRecordsInMemory() {
        return config.BUFFER_RECORD_COUNT_LIMIT;
    }

    public void checkpoint() {

        //TODO: check S3 thread is still running

        //if s3 is running
        waitForAggregation();
        //else
        // throw new IllegalStateException("S3 persister thread is not running. Counts are not persisted and we should not checkpoint!");

    }

    @Override
    public byte[] fromClass(Record record) {
        return new byte[0];
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
}
