package com.github.reveewu.delayqueue.core;

/**
 * @author reveewu
 * @date 07/06/2018
 */
public interface IDelayQueue {
    /**
     * 推送数据到延时队列
     *
     * @param message
     * @throws Exception
     */
    void push(DelayMessage message) throws Exception;

    /**
     * 根据push策略推送数据到延时队列
     *
     * @param message
     * @param policy
     * @throws Exception
     */
    void push(DelayMessage message, PolicyEnum policy) throws Exception;

    /**
     * 拉取到期数据
     *
     * @param topic
     * @throws Exception
     * @return
     */
    DelayMessageExt pull(String topic) throws Exception;

    /**
     * 消费后回调
     *
     * @param messageExt
     * @param consumeStatus
     * @throws Exception
     */
    void callback(DelayMessageExt messageExt, ConsumeStatus consumeStatus) throws Exception;
}
