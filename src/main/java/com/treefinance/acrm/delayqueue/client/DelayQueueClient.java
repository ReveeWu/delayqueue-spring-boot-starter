package com.treefinance.acrm.delayqueue.client;

import com.treefinance.acrm.delayqueue.core.*;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author reveewu
 * @date 07/06/2018
 * 客户端抽象类
 * 1. 实现消息推送和消费线程
 * 2. 定义消费抽象方法
 */
@Slf4j
public class DelayQueueClient implements DisposableBean {
    private IDelayQueue delayQueue;
    private ConcurrentHashMap<String, ScheduledExecutorService> topicExecutorMap;

    public DelayQueueClient(IDelayQueue delayQueue) {
        this.delayQueue = delayQueue;
        this.topicExecutorMap = new ConcurrentHashMap<>();
    }

    public void registerTopicListener(String topic, Function<DelayMessageExt, ConsumeStatus> function) {
        if (!topicExecutorMap.containsKey(topic)) {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
                    new DefaultThreadFactory(String.format("Delay-Queue-Client-%s-Thread", topic)));
            executorService.scheduleWithFixedDelay(() ->
                    pullThreadHandler(topic, function), 5, 1, TimeUnit.MILLISECONDS);
            topicExecutorMap.put(topic, executorService);
        }
    }

    /**
     * 推送消息
     *
     * @param message
     * @throws Exception
     */
    public void push(DelayMessage message) throws Exception {
        delayQueue.push(message);
    }

    /**
     * 根据push策略推送消息
     *
     * @param message
     * @param policy
     * @throws Exception
     */
    public void push(DelayMessage message, PolicyEnum policy) throws Exception {
        delayQueue.push(message, policy);
    }

    /**
     * 消费端拉取后台线程
     */
    private void pullThreadHandler(String topic, Function<DelayMessageExt, ConsumeStatus> function) {
        DelayMessageExt delayMessageExt;
        try {
            delayMessageExt = delayQueue.pull(topic);
            if (null == delayMessageExt) {
                return;
            }
        } catch (Exception e) {
            log.error("拉取延时队列消息出错, topic: {}", topic, e);
            return;
        }

        try {
            ConsumeStatus consumeStatus = function.apply(delayMessageExt);
            delayQueue.callback(delayMessageExt, consumeStatus);

        } catch (Exception e) {
            log.error("消费延时队列消息出错, topic: {}", topic, e);
            try {
                delayQueue.callback(delayMessageExt, ConsumeStatus.RECONSUME);
            } catch (Exception e1) {
                log.error("异常RECONSUME 出错，由回收队列处理, topic: {}", topic, e);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        try {
            topicExecutorMap.forEach((k, v) -> v.shutdown());
        } catch (Exception e) {
            log.error("DelayQueueClientShutdown error", e);
        }
    }
}
