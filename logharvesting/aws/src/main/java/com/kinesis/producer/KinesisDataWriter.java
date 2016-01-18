package com.kinesis.producer;

import java.nio.ByteBuffer;
import java.util.List;
import javax.inject.Inject;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.AWSPropertyConfiguration;
import com.amazonaws.services.kinesis.producer.Attempt;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.Metric;
import com.amazonaws.services.kinesis.producer.UserRecordFailedException;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris_ge
 */
public class KinesisDataWriter<T> {

    private static final Logger log = LoggerFactory.getLogger(KinesisDataWriter.class);
    private final static ObjectMapper JSON = new ObjectMapper();
    //could be changed to injection
    private final KinesisProducer producer = ProducerFactory.getKinesisProducer();

    private final String STREAM;

    @Inject
    public KinesisDataWriter(AWSPropertyConfiguration config) {
        this.STREAM = config.KINESIS_INPUT_STREAM;

        if (STREAM == null || STREAM.isEmpty()) {
            throw new IllegalArgumentException("stream name must not be null or empty");
        }
    }

    private byte[] toJsonAsBytes(T le) {

        try {
            return JSON.writeValueAsBytes(le);
        } catch (Throwable e) {
            return null;
        }

    }

    /**
     * no log
     */
    public void write(T le) {

        byte[] bytes = toJsonAsBytes(le);

        if (bytes == null) {
            log.error("Could not get JSON bytes for log entry: " + le);
            return;
        }
        producer.addUserRecord(STREAM, Long.toString(System.currentTimeMillis()), ByteBuffer.wrap(bytes));
    }

    /**
     * with log
     */
    public void asycWrite(T les) {
        asycWrite(Lists.newArrayList(les));
    }

    /**
     * use this when do batch write
     */
    public void asycWrite(List<T> les) {
        final FutureCallback<UserRecordResult> callback = new FutureCallback<UserRecordResult>() {
            @Override
            public void onSuccess(UserRecordResult result) {
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof UserRecordFailedException) {
                    Attempt last = Iterables.getLast(((UserRecordFailedException) t).getResult().getAttempts());
                    log.error(String.format("Record failed to put - %s : %s", last.getErrorCode(), last.getErrorMessage()));
                }
            }
        };

        for (T entry : les) {
            byte[] bytes = toJsonAsBytes(entry);

            if (bytes == null) {
                log.error("Could not get JSON bytes for log entry: " + entry);
                return;
            }
            //or you can use the other addUserRecord. The hash value used to explicitly determine the shard the data
            //record is assigned to by overriding the partition key hash（）.
            ListenableFuture<UserRecordResult> future = producer.addUserRecord(STREAM, Long.toString(System.currentTimeMillis()), ByteBuffer.wrap(bytes));
            Futures.addCallback(future, callback);
        }

    }

    public void shutDown() {
        // If you need to shutdown your application, call flushSync() first to
        // send any buffered records. This method will block until all records
        // have finished (either success or fail). There are also asynchronous
        // flush methods available.
        //
        // Records are also automatically flushed by the KPL after a while based
        // on the time limit set with Configuration.setRecordMaxBufferedTime()
        log.info("Waiting for remaining puts to finish...");
        while (producer.getOutstandingRecordsCount() > 0) {
            producer.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        log.info("All records complete.");

        // This kills the child process and shuts down the threads managing it.
        producer.destroy();
        log.info("Finished shutdown.");
    }

    // Numerous metrics are available from the KPL locally, as
    // well as uploaded to CloudWatch. See the metrics
    // documentation for details.
    //
    // KinesisProducer provides methods to retrieve metrics for
    // the current instance, with a customizable time window.
    // This allows us to get sliding window statistics in real
    // time for the current host.
    //
    // Here we're going to look at the number of user records
    // put over a 5 seconds sliding window.
    public String getMetricWithSlidingWindow(int sec) {
        String metric = null;
        try {
            for (Metric m : producer.getMetrics("UserRecordPut", sec)) {
                // Metrics are emitted at different granularities, here
                // we only look at the stream level metric, which has a
                // single dimension of stream name.
                if (m.getDimensions().size() == 1 && m.getSampleCount() > 0) {
                    metric += String.format(
                            "(Sliding 5 seconds) Avg put rate: %.2f per sec, success rate: %.2f, failure rate: %.2f, total attemped: %d, ",
                            m.getSum() / 5,
                            m.getSum() / m.getSampleCount() * 100,
                            (m.getSampleCount() - m.getSum()) / m.getSampleCount() * 100,
                            (long) m.getSampleCount());

                }
            }
        } catch (Exception e) {
            log.error("Unexpected error getting metrics", e);
        }
        return metric;
    }

    public String getAggregatedMetricForEachShard() {
        String metric = null;
        try {
            for (Metric m : producer.getMetrics("UserRecordsPerKinesisRecord")) {
                if (m.getDimensions().containsKey("ShardId")) {
                    log.info(String.format(
                            "%.2f user records were aggregated into each Kinesis record on average for shard %s, for a total of %d Kinesis records. ",
                            m.getMean(),
                            m.getDimensions().get("ShardId"),
                            (long) m.getSampleCount()));
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error getting metrics", e);
        }
        return metric;
    }
}
