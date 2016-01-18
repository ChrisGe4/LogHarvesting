package com.awesomeproject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author chris_ge
 */
public class Test2 {

    private ScheduledExecutorService aggregatorService;
    private boolean shutdown = false;

    public void init() {

        aggregatorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        });
        aggregatorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {


                    aggregate(1);


            }
        }, 1, 3, TimeUnit.MILLISECONDS);

    }

    private void aggregate(int i) {
        System.out.println("testing " + i);
    }

    public void shutdown() {
        //aggregatorService.shutdown();

        shutdown = true;
    }

}
