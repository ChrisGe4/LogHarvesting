package com.kinesis.consumer;

import java.io.IOException;
import java.util.List;
import com.AWSPropertyConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kinesis.aggregator.KinesisRecordsAggregator;
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
public class KinesisRecordProcessorTwoThreads<T> implements IRecordProcessor {

    private static final Logger log = LoggerFactory.getLogger(KinesisRecordProcessorTwoThreads.class);
    // Backoff and retry settings
    private static final long BACKOFF_TIME_IN_MILLIS = 3000L;
    // Checkpointing interval
    private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L; // 1 minute
    private static int NUM_RETRIES;
    private final ObjectMapper JSON;

    // Reporting interval
    // private static final long REPORTING_INTERVAL_MILLIS = 60000L; // 1 minute
    // private long nextReportingTimeInMillis;
    private final Class<T> recordType;
    private final KinesisRecordsAggregator aggregator;
    private String kinesisShardId;
    private long nextCheckpointTimeInMillis;
    private AWSPropertyConfiguration config;
    // private final int MAX_RECORDS_TO_WRITE;

    public KinesisRecordProcessorTwoThreads(KinesisRecordsAggregator aggregator, AWSPropertyConfiguration config, Class<T> recordType) {
        if (recordType == null) {
            throw new NullPointerException("recordType must not be null");
        }
        if (aggregator == null) {
            throw new NullPointerException("aggregator must not be null");
        }
        this.aggregator = aggregator;
        this.recordType = recordType;
        this.config = config;
        NUM_RETRIES = config.RETRY_LIMIT;
        //this.MAX_RECORDS_TO_WRITE = aggregator.getMaxRecordsInMemory();
        JSON = new ObjectMapper();
        JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void initialize(InitializationInput initializationInput) {

        this.kinesisShardId = initializationInput.getShardId();
        log.info("Initializing record processor for shard: " + kinesisShardId);
        // nextReportingTimeInMillis = System.currentTimeMillis() + REPORTING_INTERVAL_MILLIS;
        nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
        aggregator.initialize(kinesisShardId);
    }

    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
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
        /*if (System.currentTimeMillis() > nextReportingTimeInMillis) {

            nextReportingTimeInMillis = System.currentTimeMillis() + REPORTING_INTERVAL_MILLIS;
        }*/

        // Checkpoint once every checkpoint interval
        if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {

            checkpoint(processRecordsInput.getCheckpointer(), records.get(records.size() - 1).getSequenceNumber());
            nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
        }

    }

    @Override
    public void shutdown(ShutdownInput shutdownInput) {
        log.info("Shutting down record processor for shard: " + kinesisShardId);
        try {
            aggregator.shutdown();
            // Important to checkpoint after reaching end of shard, so we can initialize processing data from child shards.
            if (shutdownInput.getShutdownReason() == ShutdownReason.TERMINATE) {
                checkpoint(shutdownInput.getCheckpointer(), null);
            }

            //if there is a thread, always make sure it is killed
        } catch (Throwable t) {
            // We failed to shutdown cleanly, do not checkpoint.
            log.error("Couldn't successfully process data within the max wait time. Skip checkpointing.");
        }

    }

    private void processRecord(Record record) throws InterruptedException {
        //T entry = fromJsonAsBytes(record.getData().array());
        // here we can change the arguments according to requirements, normally will pass a pojo
        aggregator.consumeRecord(record, record.getData().array().length, record.getSequenceNumber());
        /*
        if (entry == null) {
            log.warn("Skipping record. Unable to parse record into " + recordType + ". Partition Key: " + record.getPartitionKey());
            return;
        }
        */

    }

    private void checkpoint(IRecordProcessorCheckpointer checkpointer, String SeqNumber) {
        log.info("Checkpointing shard " + kinesisShardId);
        for (int i = 0; i < NUM_RETRIES; i++) {
            try {
                aggregator.checkpoint();
                /**Using {@link IRecordProcessorCheckpointer#checkpoint()} in processRecords
                 will result in an {@link UnsupportedOperationException}.*/
                if (SeqNumber == null)
                    checkpointer.checkpoint();
                else
                    checkpointer.checkpoint(SeqNumber);
                break;
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
            } catch (InterruptedException e) {
                log.error(e.toString());
            }

            try {
                Thread.sleep(config.BACKOFF_INTERVAL);
            } catch (InterruptedException e) {
                log.debug("Interrupted sleep", e);
            }
        }
        log.error("Couldn't successfully process data within the max wait time. Skip checkpointing.");

        throw new RuntimeException(" We failed to checkpoint, so do not checkpoint.");
    }

    public T fromJsonAsBytes(byte[] bytes) {
        try {
            return JSON.readValue(bytes, recordType);
        } catch (IOException e) {
            return null;
        }
    }
}
