/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.practices.LogEntryKinesisToS3.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates random stock trades by picking randomly from a collection of stocks, assigning a
 * random price based on the mean, and picking a random quantity for the shares.
 */

public class LogDataGenerator {

    private static final List<String> fromHost = new ArrayList<String>();

    static {
        fromHost.add("http://www.amazon.com");
        fromHost.add("http://www.google.com");
        fromHost.add("http://www.yahoo.com");
        fromHost.add("http://www.bing.com");
        fromHost.add("http://www.stackoverflow.com");
        fromHost.add("http://www.reddit.com");
    }

    /**
     * The value of log type  is picked randomly between 1 and the LOG_TYPE_NUM *
     */
    private static final int LOG_TYPE_NUM = 3;

    //or use ThreadLocalRandom
    private final Random random = new Random();
    private AtomicLong id = new AtomicLong(1);

    /**
     * Return a random stock trade with a unique id every time.
     */
    public LogEntry create() {
        // pick a random host
        String from = fromHost.get(random.nextInt(fromHost.size()));

        LogType type = LogType.fromValue(ThreadLocalRandom.current().nextInt(LOG_TYPE_NUM) + 1);// add 1 because nextInt() will return between 0 (inclusive)
        // and MAX_QUANTITY (exclusive).
       /*
        * Consider a producer that experiences a network-related timeout after it makes a call to PutRecord, 
        * but before it can receive an acknowledgement from Amazon Kinesis. The producer cannot be sure if the record was delivered to Amazon Kinesis. 
        * Assuming that every record is important to the application, the producer would have been written to retry the call with the same data. 
        * If both PutRecord calls on that same data were successfully committed to Amazon Kinesis, then there will be two Amazon Kinesis records. 
        * Although the two records have identical data, they also have unique sequence numbers. Applications that need strict guarantees should 
        * embed a primary key within the record to remove duplicates later when processing. 
        * Note that the number of duplicates due to producer retries is usually low compared to the number of duplicates due to consumer retries.
        */

        return new LogEntry(UUID.randomUUID().toString(), id.getAndIncrement(), type, from, System.currentTimeMillis());
    }

}
