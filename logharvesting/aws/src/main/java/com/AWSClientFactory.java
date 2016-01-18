package com;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

/**
 * @author chris_ge
 */
public abstract class AWSClientFactory {

    private static AWSCredentialsProvider awsCredentialsProvider;

    public AWSCredentialsProvider getAWSCredentialsProvider() {


        /*String swfAccessId = "AKIAISWIC7Y4DCNWYJZQ";
        String swfSecretKey = "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";
        AWSCredentials awsCredentials = new BasicAWSCredentials(swfAccessId,
                swfSecretKey);
        return new StaticCredentialsProvider(awsCredentials);*/
        return new DefaultAWSCredentialsProviderChain();
    }
}
