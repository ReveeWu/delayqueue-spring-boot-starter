package com.treefinance.delayqueue.client.exception;

/**
 * Created by kwdfmzhu on 2017/6/12.
 */
public class DataExistsException extends RuntimeException {
    public DataExistsException() {
    }

    public DataExistsException(String message) {
        super(message);
    }

    public DataExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataExistsException(Throwable cause) {
        super(cause);
    }

    public DataExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

