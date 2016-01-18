package com.practices.LogEntryKinesisToS3.processor;

import com.AWSPropertyConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.kinesis.aggregator.KinesisRecordsAggregator;
import com.kinesis.consumer.KinesisRecordProcessor;

/**
 * @author chris_ge
 */
public class LogRecordProcessorFactory<T> implements IRecordProcessorFactory {

    private final Class<T> recordType;
    private final KinesisRecordsAggregator aggregator;
    private AWSPropertyConfiguration config;

    public LogRecordProcessorFactory(KinesisRecordsAggregator aggregator, AWSPropertyConfiguration config, Class<T> recordType) {
        if (recordType == null) {
            throw new NullPointerException("recordType must not be null");
        }
        if (aggregator == null) {
            throw new NullPointerException("aggregator must not be null");
        }
        this.recordType = recordType;
        this.aggregator = aggregator;
        this.config = config;
    }

    @Override
    public IRecordProcessor createProcessor() {
        return new KinesisRecordProcessor<T>(aggregator, config, recordType);
    }
}
