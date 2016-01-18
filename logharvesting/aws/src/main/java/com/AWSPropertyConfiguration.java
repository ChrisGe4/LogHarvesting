/*
 * Copyright 2013-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com;

import static com.config.ServiceType.KINESIS;
import static com.config.ServiceType.S3;
import java.rmi.dgc.VMID;
import java.util.Properties;
import javax.inject.Inject;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.config.PropertyConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AWSPropertyConfiguration {
    // Connector App Property Keys
    public static final String PROP_APP_NAME = "appName";
    public static final String PROP_VERSION = "version";
    public static final String PROP_CONNECTOR_DESTINATION = "connectorDestination";
    public static final String PROP_RETRY_LIMIT = "retryLimit";
    public static final String PROP_BACKOFF_INTERVAL = "backoffInterval";
    public static final String PROP_KINESIS_ENDPOINT = "kinesisEndpoint";
    public static final String PROP_KINESIS_INPUT_STREAM = "kinesisInputStream";
    public static final String PROP_KINESIS_INPUT_STREAM_SHARD_COUNT = "kinesisInputStreamShardCount";
    public static final String PROP_KINESIS_OUTPUT_STREAM = "kinesisOutputStream";
    public static final String PROP_KINESIS_OUTPUT_STREAM_SHARD_COUNT = "kinesisOutputStreamShardCount";
    public static final String PROP_WORKER_ID = "workerID";
    public static final String PROP_FAILOVER_TIME = "failoverTime";
    public static final String PROP_MAX_RECORDS = "maxRecords";
    public static final String PROP_INITIAL_POSITION_IN_STREAM = "initialPositionInStream";
    public static final String PROP_IDLE_TIME_BETWEEN_READS = "idleTimeBetweenReads";
    public static final String PROP_PARENT_SHARD_POLL_INTERVAL = "parentShardPollInterval";
    public static final String PROP_SHARD_SYNC_INTERVAL = "shardSyncInterval";
    public static final String PROP_CALL_PROCESS_RECORDS_EVEN_FOR_EMPTY_LIST = "callProcessRecordsEvenForEmptyList";
    public static final String PROP_CLEANUP_TERMINATED_SHARDS_BEFORE_EXPIRY = "cleanupTerminatedShardsBeforeExpiry";
    public static final String PROP_REGION_NAME = "regionName";
    public static final String PROP_BATCH_RECORDS_IN_PUT_REQUEST = "batchRecordsInPutRequest";
    public static final String PROP_S3_ENDPOINT = "s3Endpoint";
    public static final String PROP_S3_BUCKET = "s3Bucket";
    public static final String PROP_BUFFER_RECORD_COUNT_LIMIT = "kinesisToS3.bufferRecordCountLimit";
    public static final String PROP_BUFFER_BYTE_SIZE_LIMIT = "kinesisToS3.bufferByteSizeLimit";
    public static final String PROP_BUFFER_MILLISECONDS_LIMIT = "kinesisToS3.bufferMillisecondsLimit";
    public static final String PROP_AGGREGATOR_THREAD_INIT_DELAY = "aggregator.initialDelay";
    public static final String PROP_AGGREGATOR_THREAD_DELAY = "aggregator.delay";

    // Default App Constants
    public static final String DEFAULT_APP_NAME = "Log-Harvesting";
    public static final String DEFAULT_VERSION = "1.0";
    public static final String DEFAULT_CONNECTOR_DESTINATION = "generic";
    public static final int DEFAULT_RETRY_LIMIT = 3;
    public static final long DEFAULT_BACKOFF_INTERVAL = 1000L * 10;
    public static final int DEFAULT_BUFFER_RECORD_COUNT_LIMIT = 1000;
    public static final long DEFAULT_BUFFER_BYTE_SIZE_LIMIT = 1024 * 1024L;
    public static final long DEFAULT_BUFFER_MILLISECONDS_LIMIT = Long.MAX_VALUE;
    public static final boolean DEFAULT_BATCH_RECORDS_IN_PUT_REQUEST = false;
    public static final int DEFAULT_AGGREGATOR_THREAD_DELAY = 20;
    public static final int DEFAULT_AGGREGATOR_THREAD_INIT_DELAY = 50;

    // Default Amazon Kinesis Constants
    public static final String DEFAULT_KINESIS_ENDPOINT = null;
    public static final String DEFAULT_KINESIS_INPUT_STREAM = "GAR_INSTRUMENTATION_QA";
    public static final String DEFAULT_KINESIS_OUTPUT_STREAM = "kinesisOutputStream";
    public static final int DEFAULT_KINESIS_STREAM_SHARD_COUNT = 1;
    // Default Amazon Kinesis Client Library Constants
    public static final String DEFAULT_WORKER_ID = new VMID().toString();
    public static final long DEFAULT_FAILOVER_TIME = 30000L;
    public static final int DEFAULT_MAX_RECORDS = 10000;
    public static final InitialPositionInStream DEFAULT_INITIAL_POSITION_IN_STREAM =
            InitialPositionInStream.TRIM_HORIZON;
    public static final long DEFAULT_IDLE_TIME_BETWEEN_READS = 1000L;
    public static final long DEFAULT_PARENT_SHARD_POLL_INTERVAL = 10000L;
    public static final long DEFAULT_SHARD_SYNC_INTERVAL = 60000L;
    // CALL_PROCESS_RECORDS_EVEN_FOR_EMPTY_LIST must be set to true for bufferMillisecondsLimit to work
    public static final boolean DEFAULT_CALL_PROCESS_RECORDS_EVEN_FOR_EMPTY_LIST = true;
    public static final boolean DEFAULT_CLEANUP_TERMINATED_SHARDS_BEFORE_EXPIRY = false;
    public static final String DEFAULT_REGION_NAME = "us-east-1";
    // Default Amazon S3 Constants
    public static final String DEFAULT_S3_ENDPOINT = "https://s3.amazonaws.com";
    public static final String DEFAULT_S3_BUCKET = "kinesis-bucket";
    private static final Log LOG = LogFactory.getLog(AWSPropertyConfiguration.class);
    private static final String CONNECTION_DESTINATION_PREFIX = "amazon-kinesis-to-";

    // Configurable program variables
    public final AWSCredentialsProvider AWS_CREDENTIALS_PROVIDER;
    public final String APP_NAME;
    public final String VERSION;

    public final String CONNECTOR_DESTINATION;
    public final long BACKOFF_INTERVAL;
    public final int RETRY_LIMIT;
    public final int BUFFER_RECORD_COUNT_LIMIT;
    public final long BUFFER_BYTE_SIZE_LIMIT;
    public final long BUFFER_MILLISECONDS_LIMIT;
    public final boolean BATCH_RECORDS_IN_PUT_REQUEST;
    public final int AGGREGATOR_THREAD_INIT_DELAY;
    public final int AGGREGATOR_THREAD_DELAY;

    public final String KINESIS_ENDPOINT;
    public final String KINESIS_INPUT_STREAM;
    public final int KINESIS_INPUT_STREAM_SHARD_COUNT;
    public final String KINESIS_OUTPUT_STREAM;
    public final int KINESIS_OUTPUT_STREAM_SHARD_COUNT;

    public final String WORKER_ID;
    public final long FAILOVER_TIME;
    public final int MAX_RECORDS;
    public final InitialPositionInStream INITIAL_POSITION_IN_STREAM;
    public final long IDLE_TIME_BETWEEN_READS;
    public final long PARENT_SHARD_POLL_INTERVAL;
    public final long SHARD_SYNC_INTERVAL;
    public final boolean CALL_PROCESS_RECORDS_EVEN_FOR_EMPTY_LIST;
    public final boolean CLEANUP_TERMINATED_SHARDS_BEFORE_EXPIRY;
    public final String REGION_NAME;
    public final String S3_ENDPOINT;
    public final String S3_BUCKET;

    /**
     * Configure the connector application with any set of properties that are unique to the application. Any
     * unspecified property will be set to a default value.
     */
    @Inject
    public AWSPropertyConfiguration(
            PropertyConfiguration propConfig, AWSCredentialsProvider credentialsProvider) {
        AWS_CREDENTIALS_PROVIDER = credentialsProvider;
        Properties properties = propConfig.getProjectSetupProperties();
        // Connector configuration
        APP_NAME = properties.getProperty(PROP_APP_NAME, DEFAULT_APP_NAME);
        VERSION = properties.getProperty(PROP_VERSION, DEFAULT_VERSION);

        CONNECTOR_DESTINATION =
                CONNECTION_DESTINATION_PREFIX
                        + properties.getProperty(PROP_CONNECTOR_DESTINATION, DEFAULT_CONNECTOR_DESTINATION);
        RETRY_LIMIT = getIntegerProperty(PROP_RETRY_LIMIT, DEFAULT_RETRY_LIMIT, properties);
        BACKOFF_INTERVAL = getLongProperty(PROP_BACKOFF_INTERVAL, DEFAULT_BACKOFF_INTERVAL, properties);
        BUFFER_RECORD_COUNT_LIMIT =
                getIntegerProperty(PROP_BUFFER_RECORD_COUNT_LIMIT, DEFAULT_BUFFER_RECORD_COUNT_LIMIT, properties);
        BUFFER_BYTE_SIZE_LIMIT =
                getLongProperty(PROP_BUFFER_BYTE_SIZE_LIMIT, DEFAULT_BUFFER_BYTE_SIZE_LIMIT, properties);
        BUFFER_MILLISECONDS_LIMIT =
                getLongProperty(PROP_BUFFER_MILLISECONDS_LIMIT, DEFAULT_BUFFER_MILLISECONDS_LIMIT, properties);
        BATCH_RECORDS_IN_PUT_REQUEST =
                getBooleanProperty(PROP_BATCH_RECORDS_IN_PUT_REQUEST, DEFAULT_BATCH_RECORDS_IN_PUT_REQUEST, properties);

        AGGREGATOR_THREAD_INIT_DELAY = getIntegerProperty(PROP_AGGREGATOR_THREAD_INIT_DELAY, DEFAULT_AGGREGATOR_THREAD_INIT_DELAY, properties);
        AGGREGATOR_THREAD_DELAY = getIntegerProperty(PROP_AGGREGATOR_THREAD_DELAY, DEFAULT_AGGREGATOR_THREAD_DELAY, properties);

        properties = propConfig.getProperties(KINESIS.getType());

        // Amazon Kinesis configuration
        KINESIS_ENDPOINT = properties.getProperty(PROP_KINESIS_ENDPOINT, DEFAULT_KINESIS_ENDPOINT);
        KINESIS_INPUT_STREAM = properties.getProperty(PROP_KINESIS_INPUT_STREAM, DEFAULT_KINESIS_INPUT_STREAM);
        KINESIS_INPUT_STREAM_SHARD_COUNT =
                getIntegerProperty(PROP_KINESIS_INPUT_STREAM_SHARD_COUNT,
                        DEFAULT_KINESIS_STREAM_SHARD_COUNT,
                        properties);
        KINESIS_OUTPUT_STREAM = properties.getProperty(PROP_KINESIS_OUTPUT_STREAM, DEFAULT_KINESIS_OUTPUT_STREAM);
        KINESIS_OUTPUT_STREAM_SHARD_COUNT =
                getIntegerProperty(PROP_KINESIS_OUTPUT_STREAM_SHARD_COUNT,
                        DEFAULT_KINESIS_STREAM_SHARD_COUNT,
                        properties);

        // Amazon Kinesis Client Library configuration
        WORKER_ID = properties.getProperty(PROP_WORKER_ID, DEFAULT_WORKER_ID);
        FAILOVER_TIME = getLongProperty(PROP_FAILOVER_TIME, DEFAULT_FAILOVER_TIME, properties);
        MAX_RECORDS = getIntegerProperty(PROP_MAX_RECORDS, DEFAULT_MAX_RECORDS, properties);
        INITIAL_POSITION_IN_STREAM =
                getInitialPositionInStreamProperty(PROP_INITIAL_POSITION_IN_STREAM,
                        DEFAULT_INITIAL_POSITION_IN_STREAM,
                        properties);
        IDLE_TIME_BETWEEN_READS =
                getLongProperty(PROP_IDLE_TIME_BETWEEN_READS, DEFAULT_IDLE_TIME_BETWEEN_READS, properties);
        PARENT_SHARD_POLL_INTERVAL =
                getLongProperty(PROP_PARENT_SHARD_POLL_INTERVAL, DEFAULT_PARENT_SHARD_POLL_INTERVAL, properties);
        SHARD_SYNC_INTERVAL = getLongProperty(PROP_SHARD_SYNC_INTERVAL, DEFAULT_SHARD_SYNC_INTERVAL, properties);
        CALL_PROCESS_RECORDS_EVEN_FOR_EMPTY_LIST =
                getBooleanProperty(PROP_CALL_PROCESS_RECORDS_EVEN_FOR_EMPTY_LIST,
                        DEFAULT_CALL_PROCESS_RECORDS_EVEN_FOR_EMPTY_LIST,
                        properties);
        CLEANUP_TERMINATED_SHARDS_BEFORE_EXPIRY =
                getBooleanProperty(PROP_CLEANUP_TERMINATED_SHARDS_BEFORE_EXPIRY,
                        DEFAULT_CLEANUP_TERMINATED_SHARDS_BEFORE_EXPIRY,
                        properties);
        REGION_NAME = properties.getProperty(PROP_REGION_NAME, DEFAULT_REGION_NAME);
        properties = propConfig.getProperties(S3.getType());
        // Amazon S3 configuration
        S3_ENDPOINT = properties.getProperty(PROP_S3_ENDPOINT, DEFAULT_S3_ENDPOINT);
        S3_BUCKET = properties.getProperty(PROP_S3_BUCKET, DEFAULT_S3_BUCKET);
    }

    private boolean getBooleanProperty(String property, boolean defaultValue, Properties properties) {
        String propertyValue = properties.getProperty(property, Boolean.toString(defaultValue));
        return Boolean.parseBoolean(propertyValue);
    }

    private long getLongProperty(String property, long defaultValue, Properties properties) {
        String propertyValue = properties.getProperty(property, Long.toString(defaultValue));
        try {
            return Long.parseLong(propertyValue.trim());
        } catch (NumberFormatException e) {
            LOG.error(e);
            return defaultValue;
        }
    }

    private int getIntegerProperty(String property, int defaultValue, Properties properties) {
        String propertyValue = properties.getProperty(property, Integer.toString(defaultValue));
        try {
            return Integer.parseInt(propertyValue.trim());
        } catch (NumberFormatException e) {
            LOG.error(e);
            return defaultValue;
        }
    }

    private char getCharacterProperty(String property, char defaultValue, Properties properties) {
        String propertyValue = properties.getProperty(property, Character.toString(defaultValue));
        if (propertyValue.length() == 1) {
            return propertyValue.charAt(0);
        }
        return defaultValue;
    }

    private InitialPositionInStream getInitialPositionInStreamProperty(String property,
                                                                       InitialPositionInStream defaultInitialPositionInInputStream,
                                                                       Properties properties) {
        String propertyValue = properties.getProperty(property, defaultInitialPositionInInputStream.toString());
        try {
            return InitialPositionInStream.valueOf(propertyValue);
        } catch (Exception e) {
            LOG.error(e);
            return defaultInitialPositionInInputStream;
        }
    }
}
