package com.practices.LogEntryKinesisToS3.processor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practices.LogEntryKinesisToS3.model.LogDataGenerator;
import com.practices.LogEntryKinesisToS3.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the Kinesis Client Library (KCL) to continuously consume and process stock trade
 * records from the stock trades stream. KCL monitors the number of shards and creates
 * record processor instances to read and process records from each shard. KCL also
 * load balances shards across all the instances of this processor.
 *
 * @author chris_ge
 */
public class LogEntryRecordProcessorTest<T> {

    private static final Logger log = LoggerFactory.getLogger(LogEntryRecordProcessorTest.class);
    private final static ObjectMapper JSON = new ObjectMapper();
    // Backoff and retry settings
    private static final long BACKOFF_TIME_IN_MILLIS = 3000L;
    private static final int NUM_RETRIES = 3;
    // Reporting interval
    private static final long REPORTING_INTERVAL_MILLIS = 60000L; // 1 minute
    // Checkpointing interval
    private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L; // 1 minute
    //KinesisRecordsAggregator aggregator = new LogEntryKinesisToS3RecordsAggregator();
    private ScheduledExecutorService readDataService = Executors.newScheduledThreadPool(1);
    private LogDataGenerator generator = new LogDataGenerator();
    private String kinesisShardId;
    private long nextReportingTimeInMillis;
    private long nextCheckpointTimeInMillis;

    public static void main(String[] args) throws InterruptedException {

        LogEntryRecordProcessorTest test = new LogEntryRecordProcessorTest();
        test.initialize();
        test.start();
    }

    //private  Class<T> recordType = ;
    public void initialize() {
        //aggregator.initialize();
        log.info("Initializing record processor for shard: " + kinesisShardId);
        nextReportingTimeInMillis = System.currentTimeMillis() + REPORTING_INTERVAL_MILLIS;
        nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
    }

    public void start() throws InterruptedException {
        while (true) {
            LogEntry le = generator.create();
            //aggregator.write(le);
            System.out.println("writing = " + le);

        }
        /*readDataService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        LogEntry le =generator.create();
                        aggregator.write(le,40);

                        //aggregator.waitForAggregation();

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });*/

    }

    public void processRecords(ProcessRecordsInput processRecordsInput) {
        ReentrantLock a = new ReentrantLock(true);
        List<Record> records = processRecordsInput.getRecords();

        for (Record record : records) {
            boolean processedSuccessfully = false;
            for (int i = 0; i < NUM_RETRIES; i++) {
                try {
                    processRecord(record);
                    processedSuccessfully = true;
                    break;
                } catch (Throwable t) {
                    log.warn("Caught throwable while processing record " + record, t);
                }
                // backoff if we encounter an exception.
                try {
                    Thread.sleep(BACKOFF_TIME_IN_MILLIS);
                } catch (InterruptedException e) {
                    log.debug("Interrupted sleep", e);
                }
            }
            if (!processedSuccessfully) {
                log.error("Couldn't process record " + record + ". Skipping the record.");
            }
        }
        // If it is time to report stats as per the reporting interval, report stats
        if (System.currentTimeMillis() > nextReportingTimeInMillis) {

            nextReportingTimeInMillis = System.currentTimeMillis() + REPORTING_INTERVAL_MILLIS;
        }

        // Checkpoint once every checkpoint interval
        if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
            checkpoint(processRecordsInput.getCheckpointer());
            nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
        }

    }

    public void shutdown(ShutdownInput shutdownInput) {
        log.info("Shutting down record processor for shard: " + kinesisShardId);
        // Important to checkpoint after reaching end of shard, so we can initialize processing data from child shards.
        if (shutdownInput.getShutdownReason() == ShutdownReason.TERMINATE) {
            checkpoint(shutdownInput.getCheckpointer());
        }
    }

    private void processRecord(Record record) {
        LogEntry entry = fromJsonAsBytes(record.getData().array());
        if (entry == null) {
            log.warn("Skipping record. Unable to parse record into LogEntry. Partition Key: " + record.getPartitionKey());
            return;
        }

    }

    private void checkpoint(IRecordProcessorCheckpointer checkpointer) {
        log.info("Checkpointing shard " + kinesisShardId);
        for (int i = 0; i < NUM_RETRIES; i++) {
            try {
                checkpointer.checkpoint();
            } catch (ShutdownException se) {
                // Ignore checkpoint if the processor instance has been shutdown (fail over).
                log.info("Caught shutdown exception, skipping checkpoint.", se);
                break;
            } catch (ThrottlingException e) {
                // Backoff and re-attempt checkpoint upon transient failures
                if (i >= (NUM_RETRIES - 1)) {
                    log.error("Checkpoint failed after " + (i + 1) + "attempts.", e);
                    break;
                } else {
                    log.info("Transient issue when checkpointing - attempt " + (i + 1) + " of "
                            + NUM_RETRIES, e);
                }
            } catch (InvalidStateException e) {
                // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
                log.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
                break;
            }

            try {
                Thread.sleep(BACKOFF_TIME_IN_MILLIS);
            } catch (InterruptedException e) {
                log.debug("Interrupted sleep", e);
            }
        }
    }

    public LogEntry fromJsonAsBytes(byte[] bytes) {
        try {
            return JSON.readValue(bytes, LogEntry.class);
        } catch (IOException e) {
            return null;
        }
    }
}
