package com.treefinance.acrm.delayqueue.client;

import com.treefinance.acrm.delayqueue.core.ConsumeStatus;
import com.treefinance.acrm.delayqueue.core.DelayMessage;
import com.treefinance.acrm.delayqueue.core.DelayMessageExt;
import com.treefinance.acrm.delayqueue.core.IDelayQueue;
import com.treefinance.acrm.delayqueue.core.exception.DelayQueueException;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author reveewu
 * @date 07/06/2018
 * 客户端抽象类
 * 1. 实现消息推送和消费线程
 * 2. 定义消费抽象方法
 */
@Slf4j
public abstract class DelayQueueClient implements DisposableBean {
    private IDelayQueue delayQueue;
    private ScheduledExecutorService executorService;

    public DelayQueueClient(IDelayQueue delayQueue) {
        this.delayQueue = delayQueue;

        executorService = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory(String.format("Delay-Queue-Client-%s-Thread", getTopic())));
        executorService.scheduleWithFixedDelay(() -> pullThreadHandler(), 5, 0, TimeUnit.SECONDS);
    }

    /**
     * topic
     *
     * @return
     */
    abstract String getTopic();

    /**
     * 消费业务
     *
     * @param message
     * @return
     * @throws Exception
     */
    abstract ConsumeStatus consumeHandler(DelayMessageExt message) throws Exception;

    /**
     * 推送消息
     *
     * @param message
     * @throws DelayQueueException
     */
    public void push(DelayMessage message) throws DelayQueueException {
        delayQueue.push(message);
    }

    /**
     * 消费端拉取后台线程
     */
    private void pullThreadHandler() {
        String topic = this.getTopic();
        DelayMessageExt delayMessageExt;
        try {
            delayMessageExt = delayQueue.pull(topic);
            if (null == delayMessageExt) {
                return;
            }
        } catch (Exception e) {
            log.error("拉取延时队列消息出错, topic: {} {}", topic, e);
            return;
        }

        try {
            ConsumeStatus consumeStatus = this.consumeHandler(delayMessageExt);
            delayQueue.callback(delayMessageExt, consumeStatus);

        } catch (Exception e) {
            log.error("消费延时队列消息出错, topic: {} {}", topic, e);
            delayQueue.callback(delayMessageExt, ConsumeStatus.RECONSUME);
        }
    }

    @Override
    public void destroy() throws Exception {
        try {
            executorService.shutdown();
        } catch (Exception e) {
            log.error("DelayQueueServiceShutdownHook error", e);
        }
    }
}
