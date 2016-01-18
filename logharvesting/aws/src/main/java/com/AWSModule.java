package com;

import javax.inject.Singleton;
import com.google.inject.AbstractModule;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.kinesis.aggregator.KinesisRecordsAggregator;
import com.kinesis.consumer.KinesisExecutor;
import com.practices.LogEntryKinesisToS3.LogEntryKinesisToS3Executor;
import com.practices.LogEntryKinesisToS3.processor.DataEmitter;
import com.practices.LogEntryKinesisToS3.processor.KinesisToS3Emitter;
import com.practices.LogEntryKinesisToS3.processor.LogEntryKinesisToS3RecordsAggregator;
import com.practices.LogEntryKinesisToS3.processor.LogEntryRecordProcessorFactoryProvider;
import com.practices.LogEntryKinesisToS3.processor.LogRecordProcessorFactory;
import com.s3.S3ClientProvider;

/**
 * @author chris_ge
 */
public class AWSModule extends AbstractModule {

    @Override
    protected void configure() {

        //bind(AWSPropertyConfiguration.class).in(Singleton.class);
        //bind(AWSCredentialsProvider.class).to(DefaultAWSCredentialsProviderChain.class).in(Singleton.class);
        // bind(KinesisRecordsAggregatorTwoThreads.class).to(LogEntryKinesisToS3RecordsAggregatorTwoThreads.class);
        bind(DataEmitter.class).to(KinesisToS3Emitter.class);
        bind(KinesisRecordsAggregator.class).to(LogEntryKinesisToS3RecordsAggregator.class);
        //providers
        bind(AWSCredentialsProvider.class).toProvider(AWSCredentialsInstanceProvider.class).in(Singleton.class);
        bind(AWSPropertyConfiguration.class).toProvider(AWSPropertyConfigurationProvider.class).in(Singleton.class);

        bind(LogRecordProcessorFactory.class).toProvider(LogEntryRecordProcessorFactoryProvider.class);
        bind(AmazonS3Client.class).toProvider(S3ClientProvider.class);
        bind(KinesisExecutor.class).to(LogEntryKinesisToS3Executor.class);
    }
}
