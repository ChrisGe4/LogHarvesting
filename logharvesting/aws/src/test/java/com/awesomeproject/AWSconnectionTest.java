package com.awesomeproject;

import javax.inject.Inject;
import com.AWSModule;
import com.AWSPropertyConfigurationProvider;
import com.core.CoreServicesModule;
import com.kinesis.utils.KinesisConnectionChecker;
import com.s3.utils.S3ConnectionChecker;
import com.test.GuiceJUnitRunner;
import com.test.Modules;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author chris_ge
 */
@RunWith(GuiceJUnitRunner.class)
@Modules({AWSModule.class, CoreServicesModule.class})
public class AWSconnectionTest {

    @Inject
    AWSPropertyConfigurationProvider awsConfigProvider;

    @Test
    public void kinesisConnectionTest() {
        KinesisConnectionChecker checker = new KinesisConnectionChecker(awsConfigProvider.get());
        checker.checkStreamAvailability();

    }

    @Test
    public void s3ConnectionTest() {
        S3ConnectionChecker checker = new S3ConnectionChecker(awsConfigProvider.get());
        if (!checker.bucketExists()) {
            Assert.fail();
        }
    }

}
