package com.s3.utils;

import com.AWSPropertyConfiguration;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris_ge
 */
public class S3ConnectionChecker {

    private static final Logger log = LoggerFactory.getLogger(S3ConnectionChecker.class);

    private AmazonS3Client s3Client;
    private AWSPropertyConfiguration config;

    public S3ConnectionChecker(AWSPropertyConfiguration config) {
        this.config = config;
        this.s3Client = new AmazonS3Client(config.AWS_CREDENTIALS_PROVIDER);
        s3Client.setEndpoint(config.S3_ENDPOINT);

    }

    /**
     * @param client     The {@link AmazonS3Client} with read permissions
     * @param bucketName Check if this bucket exists
     * @return true if the Amazon S3 bucket exists, otherwise return false
     */
    public boolean bucketExists(AmazonS3Client client, String bucketName) {
        return client.doesBucketExist(bucketName);
    }

    public boolean bucketExists() {
        return bucketExists(this.s3Client, this.config.S3_BUCKET);
    }

    /**
     * Create an Amazon S3 bucket if it does not exist.
     *
     * @param client     The {@link AmazonS3Client} with read and write permissions
     * @param bucketName The bucket to create
     * @throws IllegalStateException The bucket is not created before timeout occurs
     */
    public void createBucket(AmazonS3Client client, String bucketName) {
        if (!bucketExists(client, bucketName)) {
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
            createBucketRequest.setRegion(Region.US_Standard.toString());
            client.createBucket(createBucketRequest);
        }
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 60 * 1000;
        while (!bucketExists(client, bucketName) && endTime > System.currentTimeMillis()) {
            try {
                log.info("Waiting for Amazon S3 to create bucket " + bucketName);
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
            }
        }
        if (!bucketExists(client, bucketName)) {
            throw new IllegalStateException("Could not create bucket " + bucketName);
        }
        log.info("Created Amazon S3 bucket " + bucketName);
    }
}
