package com.practices.LogEntryKinesisToS3.processor;

import javax.inject.Inject;
import javax.inject.Provider;
import com.AWSPropertyConfiguration;
import com.kinesis.aggregator.KinesisRecordsAggregator;
import com.practices.LogEntryKinesisToS3.model.LogEntry;

/**
 * @author chris_ge
 */
public class LogEntryRecordProcessorFactoryProvider implements Provider<LogRecordProcessorFactory> {

    private KinesisRecordsAggregator aggregator;
    private Provider<AWSPropertyConfiguration> awsConfig;

    @Inject
    public LogEntryRecordProcessorFactoryProvider(KinesisRecordsAggregator aggregator, Provider<AWSPropertyConfiguration> awsConfig) {
        this.aggregator = aggregator;
        this.awsConfig = awsConfig;
    }

    @Override
    public LogRecordProcessorFactory get() {
        return new LogRecordProcessorFactory<>(aggregator, awsConfig.get(), LogEntry.class);
    }
}
