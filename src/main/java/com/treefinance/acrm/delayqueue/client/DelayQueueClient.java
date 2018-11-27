package com.treefinance.acrm.delayqueue.client;

import com.treefinance.acrm.delayqueue.core.*;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.*;
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
    private DelayQueueProperties properties;
    private Integer topicConsumerThreadCount = 1;
    private IDelayQueue delayQueue;
    private ConcurrentHashMap<String, ExecutorService> topicExecutorMap;

    public DelayQueueClient(IDelayQueue delayQueue, DelayQueueProperties properties) {
        this.properties = properties;
        this.delayQueue = delayQueue;
        this.topicConsumerThreadCount = properties.getConsumerThreadCount();
        this.topicExecutorMap = new ConcurrentHashMap<>();
    }

    public void registerTopicListener(String topic, Function<DelayMessageExt, ConsumeStatus> function) {
        if (!topicExecutorMap.containsKey(topic)) {
            ExecutorService executorService = Executors.newFixedThreadPool(this.topicConsumerThreadCount,
                    new DefaultThreadFactory(String.format("Delay-Queue-Client-%s-Thread", topic)));
            topicExecutorMap.put(topic, executorService);
            for (int i = 0; i < this.topicConsumerThreadCount; i++) {
                executorService.submit(() -> {
                    while (true) {
                        if (!pullThreadHandler(topic, function)) {
                            try {
                                // 如果出错或没有数据，休眠1秒
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                log.error("error", e);
                            }
                        }
                    }
                });
            }
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
    private boolean pullThreadHandler(String topic, Function<DelayMessageExt, ConsumeStatus> function) {
        DelayMessageExt delayMessageExt;
        try {
            delayMessageExt = delayQueue.pull(topic);
            if (null == delayMessageExt) {
                return false;
            }
        } catch (Exception e) {
            log.error("拉取延时队列消息出错, topic: {}", topic, e);
            return false;
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("topic: {}, body: {}, delay: {}s", topic, delayMessageExt.getBody(), (System.currentTimeMillis() - delayMessageExt.getExecuteTime())/1000);
            }
            ConsumeStatus consumeStatus = function.apply(delayMessageExt);
            delayQueue.callback(delayMessageExt, consumeStatus);

            return true;
        } catch (Exception e) {
            log.error("消费延时队列消息出错, topic: {}", topic, e);
            try {
                delayQueue.callback(delayMessageExt, ConsumeStatus.RECONSUME);
            } catch (Exception e1) {
                log.error("异常RECONSUME 出错，由回收队列处理, topic: {}", topic, e);
            }
            return false;
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
