package com.awesomeproject;

import static com.config.ServiceType.KINESIS;
import static com.config.ServiceType.S3;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import com.google.common.collect.ImmutableList;
import com.AWSModule;
import com.AWSPropertyConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.model.Record;
import com.config.PropertyConfiguration;
import com.core.CoreServicesModule;
import com.kinesis.aggregator.KinesisRecordsAggregator;
import com.practices.LogEntryKinesisToS3.processor.DataEmitter;
import com.practices.LogEntryKinesisToS3.processor.LogEntryKinesisToS3RecordsAggregator;
import com.test.GuiceJUnitRunner;
import com.test.Modules;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author chris_ge
 */

@RunWith(GuiceJUnitRunner.class)
@Modules({AWSModule.class, CoreServicesModule.class})
public class AggregatorTest {
    @Inject
    private AWSPropertyConfiguration config;

    private DataEmitter emitter;

    private Record record;

    private ByteBuffer byteBuffer;
    private PropertyConfiguration propertyConfig;

    private AWSCredentialsProvider credentialsProvider;

    private KinesisRecordsAggregator aggregator;
    private Properties properties;
//    @Inject
//    private Provider<AWSPropertyConfiguration> provider;

    @Before
    public void init() {

        emitter = mock(DataEmitter.class);
        propertyConfig = mock(PropertyConfiguration.class);
        credentialsProvider = mock(AWSCredentialsProvider.class);
        properties = mock(Properties.class);
        //byteBuffer = mock(ByteBuffer.class);
        byteBuffer = ByteBuffer.allocate(5000);
        byteBuffer.put("test".getBytes());
        record = mock(Record.class);
        when(propertyConfig.getProjectSetupProperties()).thenReturn(properties);
        when(propertyConfig.getProperties(KINESIS.getType())).thenReturn(properties);
        when(propertyConfig.getProperties(S3.getType())).thenReturn(properties);
        when(properties.getProperty(eq("bufferByteSizeLimit"), anyString())).thenReturn("5000");
        when(properties.getProperty(eq("bufferRecordCountLimit"), anyString())).thenReturn("5");
        when(properties.getProperty(eq("bufferMillisecondsLimit"), anyString())).thenReturn("5000");
        when(record.getData()).thenReturn(byteBuffer);
        //when(byteBuffer.array()).thenReturn("test".getBytes());
//        doReturn("test".getBytes()).when(byteBuffer).array();
        // config = new AWSPropertyConfiguration(propertyConfig, credentialsProvider);
        //config = provider.get();
        aggregator = new LogEntryKinesisToS3RecordsAggregator(config, emitter);

    }

    @Test
    public void consumeRecordTest() throws IOException {
        for (int i = 1; i <= config.BUFFER_RECORD_COUNT_LIMIT; i++) {
            aggregator.consumeRecord(record, 100, "seq" + i);
        }

        assertEquals(aggregator.getFirstSequenceNumber(), "seq1");
        assertEquals(aggregator.getLastSequenceNumber(), "seq" + config.BUFFER_RECORD_COUNT_LIMIT);

        assertEquals(aggregator.getBuffer().size(), config.BUFFER_RECORD_COUNT_LIMIT);

    }

    @Test
    public void emit_success() throws IOException {
        List<byte[]> unprocessed = ImmutableList.of();
        when(emitter.emit(anyList(), anyString())).thenReturn(unprocessed);

        aggregator.emit(aggregator.getBuffer());
        verify(emitter, times(1)).emit(anyList(), anyString());
        verify(emitter, times(0)).fail(anyList());
        assertTrue(aggregator.getBuffer().isEmpty());
    }

    @Test
    public void emit_exceeds_retrylimits() throws IOException {
        List<byte[]> unprocessed = ImmutableList.of("test".getBytes());
        when(emitter.emit(anyList(), anyString())).thenReturn(unprocessed);
        aggregator.emit(aggregator.getBuffer());
        verify(emitter, times(config.RETRY_LIMIT)).emit(anyList(), anyString());
        verify(emitter, atLeast(1)).fail(anyList());
        assertTrue(aggregator.getBuffer().isEmpty());
        assertFalse(aggregator.shouldFlush());
    }

    @Test
    public void shouldFlushTest() throws IOException, InterruptedException {
        for (int i = 1; i <= config.BUFFER_RECORD_COUNT_LIMIT; i++) {
            aggregator.consumeRecord(record, 100, "seq" + i);
        }

        assertTrue(aggregator.shouldFlush());
        List<byte[]> unprocessed = ImmutableList.of();
        when(emitter.emit(anyList(), anyString())).thenReturn(unprocessed);
        aggregator.emit(aggregator.getBuffer());

        Thread.sleep(config.BUFFER_MILLISECONDS_LIMIT);

        aggregator.consumeRecord(record, 100, "seq1");
        assertTrue(aggregator.shouldFlush());

    }
}
