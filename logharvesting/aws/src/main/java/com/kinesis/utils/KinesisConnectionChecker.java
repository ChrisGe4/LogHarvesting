package com.kinesis.utils;

import java.util.List;
import com.AWSPropertyConfiguration;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamRequest;
import com.amazonaws.services.kinesis.model.ListStreamsRequest;
import com.amazonaws.services.kinesis.model.ListStreamsResult;
import com.amazonaws.services.kinesis.model.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris_ge
 */
public class KinesisConnectionChecker {
    private static final Logger log = LoggerFactory.getLogger(KinesisConnectionChecker.class);

    private AmazonKinesisClient kinesisClient;
    private AWSPropertyConfiguration config;

    public KinesisConnectionChecker(AWSPropertyConfiguration config) {
        this.config = config;
        this.kinesisClient = new AmazonKinesisClient(config.AWS_CREDENTIALS_PROVIDER);
        kinesisClient.setRegion(RegionUtils.getRegion(config.REGION_NAME));
        if (config.KINESIS_ENDPOINT != null) {
            kinesisClient.setEndpoint(config.KINESIS_ENDPOINT);
        }
    }

    public void checkStreamAvailability(AmazonKinesisClient kinesisClient,
                                        String streamName
    ) {
        if (streamExists(kinesisClient, streamName)) {
            String state = streamState(kinesisClient, streamName);
            switch (state) {
                case "DELETING":
                    long startTime = System.currentTimeMillis();
                    long endTime = startTime + 1000 * 120;
                    while (System.currentTimeMillis() < endTime && streamExists(kinesisClient, streamName)) {
                        try {
                            log.info("...Deleting Stream " + streamName + "...");
                            Thread.sleep(1000 * 10);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (streamExists(kinesisClient, streamName)) {
                        log.error("KinesisUtils timed out waiting for stream " + streamName + " to delete");
                        throw new IllegalStateException("KinesisUtils timed out waiting for stream " + streamName
                                + " to delete");
                    }
                case "ACTIVE":
                    log.info("Stream " + streamName + " is ACTIVE");
                    return;
                case "CREATING":
                    break;
                case "UPDATING":
                    log.info("Stream " + streamName + " is UPDATING");
                    return;
                default:
                    throw new IllegalStateException("Illegal stream state: " + state);
            }
        } else {
            throw new IllegalStateException(" Stream: " + streamName + " doesn't exist");

            /* create stream
            CreateStreamRequest createStreamRequest = new CreateStreamRequest();
            createStreamRequest.setStreamName(streamName);
            createStreamRequest.setShardCount(shardCount);
            kinesisClient.createStream(createStreamRequest);
            log.info("Stream " + streamName + " created");
            */
        }
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(1000 * 10);
            } catch (Exception e) {
            }
            try {
                String streamStatus = streamState(kinesisClient, streamName);
                if (streamStatus.equals("ACTIVE")) {
                    log.info("Stream " + streamName + " is ACTIVE");
                    return;
                }
            } catch (ResourceNotFoundException e) {
                throw new IllegalStateException("Stream " + streamName + " never went active");
            }
        }
    }

    public void checkStreamAvailability() {
        checkStreamAvailability(this.kinesisClient, config.KINESIS_INPUT_STREAM);
    }

    /**
     * Helper method to determine if an Amazon Kinesis stream exists.
     *
     * @param kinesisClient The {@link AmazonKinesisClient} with Amazon Kinesis read privileges
     * @param streamName    The Amazon Kinesis stream to check for
     * @return true if the Amazon Kinesis stream exists, otherwise return false
     */
    public boolean streamExists(AmazonKinesisClient kinesisClient, String streamName) {
        DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest();
        describeStreamRequest.setStreamName(streamName);
        try {
            kinesisClient.describeStream(describeStreamRequest);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    public boolean streamExists() {

        return streamExists(kinesisClient, config.KINESIS_INPUT_STREAM);
    }

    /**
     * Return the state of a Amazon Kinesis stream.
     *
     * @param kinesisClient The {@link AmazonKinesisClient} with Amazon Kinesis read privileges
     * @param streamName    The Amazon Kinesis stream to get the state of
     * @return String representation of the Stream state
     */
    public String streamState(AmazonKinesisClient kinesisClient, String streamName) {
        DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest();
        describeStreamRequest.setStreamName(streamName);
        try {
            return kinesisClient.describeStream(describeStreamRequest).getStreamDescription().getStreamStatus();
        } catch (AmazonServiceException e) {
            return null;
        }
    }

    public String streamState() {

        return streamState(kinesisClient, config.KINESIS_INPUT_STREAM);
    }

    /**
     * Gets a list of all Amazon Kinesis streams
     *
     * @param kinesisClient The {@link AmazonKinesisClient} with Amazon Kinesis read privileges
     * @return list of Amazon Kinesis streams
     */
    public static List<String> listAllStreams(AmazonKinesisClient kinesisClient) {

        ListStreamsRequest listStreamsRequest = new ListStreamsRequest();
        listStreamsRequest.setLimit(10);
        ListStreamsResult listStreamsResult = kinesisClient.listStreams(listStreamsRequest);
        List<String> streamNames = listStreamsResult.getStreamNames();
        while (listStreamsResult.isHasMoreStreams()) {
            if (streamNames.size() > 0) {
                listStreamsRequest.setExclusiveStartStreamName(streamNames.get(streamNames.size() - 1));
            }

            listStreamsResult = kinesisClient.listStreams(listStreamsRequest);
            streamNames.addAll(listStreamsResult.getStreamNames());
        }
        return streamNames;
    }

    public List<String> listAllStreams() {
        return listAllStreams(this.kinesisClient);
    }

}
