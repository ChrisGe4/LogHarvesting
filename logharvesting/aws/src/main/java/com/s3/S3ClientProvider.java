package com.s3;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import com.AWSPropertyConfiguration;
import com.amazonaws.services.s3.AmazonS3Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris_ge
 */
@Singleton
public class S3ClientProvider implements Provider<AmazonS3Client> {

    private static final Logger log = LoggerFactory.getLogger(S3ClientProvider.class);

    private final AmazonS3Client client;

    @Inject
    public S3ClientProvider(AWSPropertyConfiguration propertyConfig) {

        this.client = new AmazonS3Client(propertyConfig.AWS_CREDENTIALS_PROVIDER);
        log.info(" Amazon S3 Client created ");
        String bucket = propertyConfig.S3_BUCKET;
        if (bucket == null || client.doesBucketExist(bucket)) {
            String msg = "Bucket " + bucket + " does not exist";
            throw new IllegalStateException(msg);

        }
    }

    public AmazonS3Client get() {
        return client;
    }
}
