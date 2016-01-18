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
package com.practices.LogEntryKinesisToS3.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import com.AWSPropertyConfiguration;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This implementation of IEmitter is used to store files from an Amazon Kinesis stream in S3. The use of
 * this class requires the configuration of an Amazon S3 bucket/endpoint. When the buffer is full, this
 * class's emit method adds the contents of the buffer to Amazon S3 as one file. The filename is generated
 * from the first and last sequence numbers of the records contained in that file separated by a
 * dash. This class requires the configuration of an Amazon S3 bucket and endpoint.
 */
public class KinesisToS3Emitter implements DataEmitter {
    private static final Log LOG = LogFactory.getLog(KinesisToS3Emitter.class);
    protected final String s3Bucket;
    protected final String s3Endpoint;

    protected final AmazonS3Client s3client;

    @Inject
    public KinesisToS3Emitter(AWSPropertyConfiguration configuration, AmazonS3Client s3client) {
        s3Bucket = configuration.S3_BUCKET;
        s3Endpoint = configuration.S3_ENDPOINT;
        this.s3client = s3client;
        if (s3Endpoint != null) {
            s3client.setEndpoint(s3Endpoint);
        }
    }

    protected String getS3URI(String s3FileName) {
        return "s3://" + s3Bucket + "/" + s3FileName;
    }

    @Override
    public List<byte[]> emit(List<byte[]> records, String s3FileName) throws IOException {
        // Write all of the records to a compressed output stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] record : records) {
            try {
                baos.write(record);
            } catch (Exception e) {
                LOG.error("Error writing record to output stream. Failing this emit attempt. Record: "
                                + Arrays.toString(record),
                        e
                );
                return records;
            }
        }
        // Get the Amazon S3 filename
        String s3URI = getS3URI(s3FileName);
        try {
            ByteArrayInputStream object = new ByteArrayInputStream(baos.toByteArray());
            LOG.debug("Starting upload of file " + s3URI + " to Amazon S3 containing " + records.size() + " records.");
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(baos.size());
            s3client.putObject(s3Bucket, s3FileName, object, meta);
            LOG.info("Successfully emitted " + records.size() + " records to Amazon S3 in " + s3URI);
            return Collections.emptyList();
        } catch (Exception e) {
            LOG.error("Caught exception when uploading file " + s3URI + "to Amazon S3. Failing this emit attempt.", e);
            return records;
        }
    }

    @Override
    public void fail(List<byte[]> records) {
        for (byte[] record : records) {
            LOG.error("Record failed: " + Arrays.toString(record));
        }
    }

    @Override
    public void shutdown() {
        s3client.shutdown();
    }

}
