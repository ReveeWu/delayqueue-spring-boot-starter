package com.treefinance.delayqueue.client.enums;

import java.io.Serializable;

/**
 * @author reveewu
 * @date 04/05/2018
 * 延时消息状态
 */
public enum ConsumeStatus implements Serializable {
    /**
     * 成功
     */
    SUCCESS,
    /**
     * 失败
     */
    FAIL,
    /**
     * 重新消费
     */
    RECONSUME
}
