package com.github.reveewu.delayqueue.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author reveewu
 * @date 28/11/2018
 */
public class ThreadFactoryImpl implements ThreadFactory {
    private final AtomicLong threadIndex = new AtomicLong(0);
    private final String threadNamePrefix;


    public ThreadFactoryImpl(final String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }


    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, threadNamePrefix + this.threadIndex.incrementAndGet());
    }
}
