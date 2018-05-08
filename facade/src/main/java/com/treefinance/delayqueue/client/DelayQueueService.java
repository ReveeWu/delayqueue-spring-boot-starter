package com.treefinance.delayqueue.client;

import com.treefinance.delayqueue.client.bean.DelayMessage;
import com.treefinance.delayqueue.client.bean.DelayMessageExt;
import com.treefinance.delayqueue.client.enums.ConsumeStatus;
import com.treefinance.delayqueue.client.exception.DataExistsException;
import com.treefinance.delayqueue.client.exception.DelayQueueException;
import org.slf4j.Logger;

import java.util.function.Function;

/**
 * @author reveewu
 * @date 04/05/2018
 */
public interface DelayQueueService {
    /**
     * 推送数据到延时队列
     *
     * @param message
     * @throws DataExistsException
     * @throws DelayQueueException
     */
    void push(DelayMessage message) throws DataExistsException, DelayQueueException;

    /**
     * 拉取到期数据
     *
     * @param groupName
     * @param topic
     * @return
     */
    DelayMessageExt pull(String groupName, String topic) throws DelayQueueException;

    /**
     * 消费后回调
     *
     * @param messageExt
     * @param consumeStatus
     */
    void callback(DelayMessageExt messageExt, ConsumeStatus consumeStatus);

    /**
     * 默认消费方法
     *
     * @param groupName
     * @param topic
     * @param function
     */
    default void consume(String groupName, String topic, Function<DelayMessageExt, ConsumeStatus> function, Logger logger) {
        DelayMessageExt delayMessageExt;
        try {
            delayMessageExt = pull(groupName, topic);
            if (null == delayMessageExt) {
                return;
            }
        } catch (Exception e) {
            logger.error("拉取延时队列消息出错, groupName: {}, topic: {} {}", groupName, topic, e);
            return;
        }

        try {
            ConsumeStatus consumeStatus = function.apply(delayMessageExt);
            callback(delayMessageExt, consumeStatus);

        } catch (Exception e) {
            logger.error("消费延时队列消息出错, groupName: {}, topic: {} {}", groupName, topic, e);
            callback(delayMessageExt, ConsumeStatus.RECONSUME);
        }
    }
}
