package com.practices.LogEntryKinesisToS3;

import javax.inject.Inject;
import javax.inject.Provider;
import com.AWSPropertyConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.kinesis.consumer.KinesisExecutor;
import com.practices.LogEntryKinesisToS3.processor.LogRecordProcessorFactory;

/**
 * @author chris_ge
 */
public class LogEntryKinesisToS3Executor extends KinesisExecutor {

    private Provider<AWSPropertyConfiguration> awsConfig;
    private Provider<LogRecordProcessorFactory> processorFactoryProvider;

    @Inject
    public LogEntryKinesisToS3Executor(Provider<AWSPropertyConfiguration> awsConfig, Provider<LogRecordProcessorFactory> provider) {
        this.processorFactoryProvider = provider;
        this.awsConfig = awsConfig;

    }

    @Override
    public void initialize() {
        super.initialize(awsConfig.get());
    }

    @Override
    public IRecordProcessorFactory getKinesisRecordProcessorFactory() {
        return processorFactoryProvider.get();
    }

}
