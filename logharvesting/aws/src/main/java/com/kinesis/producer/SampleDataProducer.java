package com.kinesis.producer;

import com.practices.LogEntryKinesisToS3.model.LogDataGenerator;
import com.practices.LogEntryKinesisToS3.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris_ge
 */
public class SampleDataProducer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SampleDataProducer.class);
    private KinesisDataWriter<LogEntry> writer;
    private LogDataGenerator dataGenerator;
    private int iteration;

    public SampleDataProducer(KinesisDataWriter<LogEntry> writer, LogDataGenerator dataGenerator, int iteration) {
        this.writer = writer;
        this.dataGenerator = dataGenerator;
        this.iteration = iteration;
    }

    @Override
    public void run() {
        int loop = 0;
        do {
            writer.write(dataGenerator.create());
            loop++;
        } while (iteration != loop);

    }

}
