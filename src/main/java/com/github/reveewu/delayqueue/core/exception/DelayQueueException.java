package com.github.reveewu.delayqueue.core.exception;

/**
 * Created by kwdfmzhu on 2017/6/12.
 */
public class DelayQueueException extends RuntimeException {
    public DelayQueueException() {
    }

    public DelayQueueException(String message) {
        super(message);
    }

    public DelayQueueException(String message, Throwable cause) {
        super(message, cause);
    }

    public DelayQueueException(Throwable cause) {
        super(cause);
    }

    public DelayQueueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

