package com.kinesis.consumer;

import com.AWSPropertyConfiguration;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.amazonaws.services.kinesis.connectors.KinesisConnectorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris_ge
 */
public abstract class KinesisExecutor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(KinesisExecutor.class);
    private Worker worker = null;

    @Override
    public void run() {

        if (worker != null) {
            try {
                worker.run();
            } catch (Throwable t) {
                log.error("Caught throwable while running the kinesis consumer.", t);
                throw t;
            } finally {
                log.error("Worker " + getClass().getSimpleName() + " is not running.");
            }
        } else {

            throw new RuntimeException("Initialize must be called before run.");

        }
    }

    public void shutdown() {
        if (worker != null) {
            worker.shutdown();
        }
    }

    public abstract void initialize();

    protected void
    initialize(AWSPropertyConfiguration awsPropertyConfiguration) {
        KinesisClientLibConfiguration kinesisClientLibConfiguration =
                new KinesisClientLibConfiguration(awsPropertyConfiguration.APP_NAME,
                        awsPropertyConfiguration.KINESIS_INPUT_STREAM,
                        awsPropertyConfiguration.AWS_CREDENTIALS_PROVIDER,
                        awsPropertyConfiguration.WORKER_ID).withKinesisEndpoint(awsPropertyConfiguration.KINESIS_ENDPOINT)
                        .withFailoverTimeMillis(awsPropertyConfiguration.FAILOVER_TIME)
                        .withMaxRecords(awsPropertyConfiguration.MAX_RECORDS)
                        .withInitialPositionInStream(awsPropertyConfiguration.INITIAL_POSITION_IN_STREAM)
                        .withIdleTimeBetweenReadsInMillis(awsPropertyConfiguration.IDLE_TIME_BETWEEN_READS)
                        .withCallProcessRecordsEvenForEmptyRecordList(KinesisConnectorConfiguration.DEFAULT_CALL_PROCESS_RECORDS_EVEN_FOR_EMPTY_LIST)
                        .withCleanupLeasesUponShardCompletion(awsPropertyConfiguration.CLEANUP_TERMINATED_SHARDS_BEFORE_EXPIRY)
                        .withParentShardPollIntervalMillis(awsPropertyConfiguration.PARENT_SHARD_POLL_INTERVAL)
                        .withShardSyncIntervalMillis(awsPropertyConfiguration.SHARD_SYNC_INTERVAL)
                        .withTaskBackoffTimeMillis(awsPropertyConfiguration.BACKOFF_INTERVAL)
                        .withUserAgent(awsPropertyConfiguration.APP_NAME + ","
                                + awsPropertyConfiguration.CONNECTOR_DESTINATION + ","
                                + KinesisConnectorConfiguration.KINESIS_CONNECTOR_USER_AGENT)
                        .withRegionName(awsPropertyConfiguration.REGION_NAME)
                        .withCommonClientConfig(getClientConfigWithUserAgent(awsPropertyConfiguration));

        if (!awsPropertyConfiguration.CALL_PROCESS_RECORDS_EVEN_FOR_EMPTY_LIST) {
            log.warn("The false value of callProcessRecordsEvenForEmptyList will be ignored. It must be set to true for the bufferTimeMillisecondsLimit to work correctly.");
        }

        if (awsPropertyConfiguration.IDLE_TIME_BETWEEN_READS > awsPropertyConfiguration.BUFFER_MILLISECONDS_LIMIT) {
            log.warn("idleTimeBetweenReads is greater than bufferTimeMillisecondsLimit. For best results, ensure that bufferTimeMillisecondsLimit is more than or equal to idleTimeBetweenReads ");
        }
        worker = new Worker.Builder()
                .recordProcessorFactory(getKinesisRecordProcessorFactory())
                .config(kinesisClientLibConfiguration)
                .build();
        log.info(getClass().getSimpleName() + " worker created");
    }

    public ClientConfiguration getClientConfigWithUserAgent(AWSPropertyConfiguration awsPropertyConfiguration) {

        final ClientConfiguration config = new ClientConfiguration();
        final StringBuilder userAgent = new StringBuilder(ClientConfiguration.DEFAULT_USER_AGENT);

        // Separate fields of the user agent with a space
        userAgent.append(" ");
        // Append the application name followed by version number of the sample
        userAgent.append(awsPropertyConfiguration.APP_NAME);
        userAgent.append("/");
        userAgent.append(awsPropertyConfiguration.VERSION);

        config.setUserAgent(userAgent.toString());

        return config;
    }

    public abstract IRecordProcessorFactory getKinesisRecordProcessorFactory();
}
