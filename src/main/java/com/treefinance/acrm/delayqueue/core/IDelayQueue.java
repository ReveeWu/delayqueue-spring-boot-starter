package com.treefinance.acrm.delayqueue.core;

import com.treefinance.acrm.delayqueue.core.exception.DataExistsException;
import com.treefinance.acrm.delayqueue.core.exception.DelayQueueException;

/**
 * @author reveewu
 * @date 07/06/2018
 */
public interface IDelayQueue {
    /**
     * 推送数据到延时队列
     *
     * @param message
     * @throws DataExistsException
     * @throws DelayQueueException
     */
    void push(DelayMessage message) throws DelayQueueException;

    /**
     * 拉取到期数据
     *
     * @param topic
     * @return
     */
    DelayMessageExt pull(String topic) throws DelayQueueException;

    /**
     * 消费后回调
     *
     * @param messageExt
     * @param consumeStatus
     */
    void callback(DelayMessageExt messageExt, ConsumeStatus consumeStatus);
}
