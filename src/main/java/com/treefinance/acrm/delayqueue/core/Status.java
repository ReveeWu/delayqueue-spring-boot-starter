package com.treefinance.acrm.delayqueue.core;

import java.io.Serializable;

/**
 * @author reveewu
 * @date 04/05/2018
 * 延时消息状态
 */
public enum Status implements Serializable {
    /**
     * 待入列
     */
    WAIT_PUSH,
    /**
     * 待出列
     */
    WAIT_PULL,
    /**
     * 待消费
     */
    WAIT_CONSUME,
    /**
     * 待重新消费
     */
    WAIT_RECONSUME,
    /**
     * 消费中
     */
    CONSUMING,
    /**
     * 已消费
     */
    CONSUMED,
    /**
     * 消费失败
     */
    CONSUME_ERROR
}
