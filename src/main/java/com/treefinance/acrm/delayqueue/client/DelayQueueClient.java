package com.treefinance.acrm.delayqueue.client;

import com.treefinance.acrm.delayqueue.common.ServiceThread;
import com.treefinance.acrm.delayqueue.common.ThreadFactoryImpl;
import com.treefinance.acrm.delayqueue.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @author reveewu
 * @date 07/06/2018
 * 客户端
 * 1. 实现消息推送和消费线程
 */
@Slf4j
public class DelayQueueClient implements DisposableBean {
    private DelayQueueProperties properties;
    private IDelayQueue delayQueue;
    /**
     * topic --> consume function
     */
    private ConcurrentHashMap<String, Function<DelayMessageExt, ConsumeStatus>> topicConsumerMap;
    /**
     * topic --> PullMessageService
     */
    private ConcurrentHashMap<String, PullMessageService> topicPullMessageServiceMap;
    /**
     * 消费者线程池任务队列
     */
    private final BlockingQueue<Runnable> consumeRequestQueue;
    /**
     * 消费者线程池
     */
    private final ThreadPoolExecutor consumeExecutor;
    /**
     * 消费者线程池过载
     */
    private volatile boolean overload = false;
    /**
     * 过载重试线程
     */
    private ScheduledExecutorService retryScheduledExecutor;

    public DelayQueueClient(IDelayQueue delayQueue, DelayQueueProperties properties) {
        this.properties = properties;
        this.delayQueue = delayQueue;
        this.topicConsumerMap = new ConcurrentHashMap<>();
        this.topicPullMessageServiceMap = new ConcurrentHashMap<>();

        this.consumeRequestQueue = new LinkedBlockingQueue<>(properties.getPullThresholdCount());
        this.consumeExecutor = new ThreadPoolExecutor(
                this.properties.getConsumeThreadMin(),
                this.properties.getConsumeThreadMax(),
                1000 * 60,
                TimeUnit.MILLISECONDS,
                this.consumeRequestQueue,
                new ThreadFactoryImpl("DelayQueueConsumerThread_"),
                (r, executor) -> {
                    // 提交任务被拒处理
                    log.info("消费线程过载，任务提交被拒绝，稍后重试");
                    overload = true;
                    retryScheduledExecutor.schedule(() -> {
                        executor.submit(r);
                    }, 5, TimeUnit.SECONDS);
                });
        this.retryScheduledExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("RetryScheduledExecutor_"));
    }

    public void registerTopicListener(String topic, Function<DelayMessageExt, ConsumeStatus> function) {
        if (!topicConsumerMap.containsKey(topic)) {
            PullMessageService pullMessageService = new PullMessageService(topic);

            topicConsumerMap.put(topic, function);
            topicPullMessageServiceMap.put(topic, pullMessageService);

            pullMessageService.start();
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

    @Override
    public void destroy() throws Exception {
        try {
            topicPullMessageServiceMap.forEach((k, v) -> {
                v.stop();
                log.info("{} pull service stopped", k);
            });
        } catch (Exception e) {
            log.error("DelayQueueClientShutdown error", e);
        }
        try {
            consumeExecutor.shutdown();
            log.info("consumeExecutor shutdown");
        } catch (Exception e) {
            log.error("DelayQueueClientShutdown error", e);
        }
    }

    class PullMessageService extends ServiceThread {
        private String topic;

        public PullMessageService(String topic) {
            super(String.format("%s_%s", PullMessageService.class.getSimpleName(), topic));
            this.topic = topic;
        }

        @Override
        public void run() {
            while (!this.isStoped()) {
                try {
                    if (overload) {
                        log.info("消费线程过载，拉取线程休息一下，topic：{}", this.topic);
                        while (overload && consumeRequestQueue.remainingCapacity() < properties.getPullThresholdCount() / 2) {
                            Thread.sleep(200);
                        }
                        overload = false;
                    }

                    DelayMessageExt delayMessageExt = delayQueue.pull(topic);
                    if (null == delayMessageExt) {
                        // 如果出错或没有数据，休眠100毫秒
                        Thread.sleep(100);
                        continue;
                    }

                    consumeExecutor.submit(new ConsumeRequest(delayMessageExt));
                } catch (Exception e) {
                    log.error("拉取延时队列消息出错, topic: {}", topic, e);
                    // 如果出错或没有数据，休眠1秒
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        log.error("error", e1);
                    }
                }
            }
        }
    }

    class ConsumeRequest implements Runnable {
        private DelayMessageExt messageExt;

        public ConsumeRequest(DelayMessageExt messageExt) {
            this.messageExt = messageExt;
        }

        @Override
        public void run() {
            try {
                Function<DelayMessageExt, ConsumeStatus> fun = topicConsumerMap.get(this.messageExt.getTopic());
                if (null == fun) {
                    log.warn("找不到该topic对应的消费方法 {}", this.messageExt.getTopic());
                }

                if (log.isDebugEnabled()) {
                    log.debug("topic: {}, body: {}, delay: {}s", messageExt.getTopic(), messageExt.getBody(), (System.currentTimeMillis() - messageExt.getExecuteTime()) / 1000);
                }

                ConsumeStatus consumeStatus = fun.apply(this.messageExt);
                delayQueue.callback(this.messageExt, consumeStatus);
            } catch (Exception e) {
                log.error("消费延时队列消息出错, topic: {}", this.messageExt.getTopic(), e);
                try {
                    delayQueue.callback(this.messageExt, ConsumeStatus.RECONSUME);
                } catch (Exception e1) {
                    log.error("异常RECONSUME 出错，由回收队列处理, topic: {}", this.messageExt.getTopic(), e1);
                }
            }
        }
    }
}
