package com;

import javax.inject.Provider;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

/**
 * @author chris_ge
 */
public class AWSCredentialsInstanceProvider implements Provider<AWSCredentialsProvider> {

    @Override
    public AWSCredentialsProvider get() {
        /*String swfAccessId = "AKIAISWIC7Y4DCNWYJZQ";
        String swfSecretKey = "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";
        AWSCredentials awsCredentials = new BasicAWSCredentials(swfAccessId,
                swfSecretKey);
        return new StaticCredentialsProvider(awsCredentials);*/
        return new DefaultAWSCredentialsProviderChain();
    }
}
