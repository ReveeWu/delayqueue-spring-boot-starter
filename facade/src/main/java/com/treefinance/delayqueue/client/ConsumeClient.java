package com.treefinance.delayqueue.client;

import com.treefinance.delayqueue.client.bean.DelayMessageExt;
import com.treefinance.delayqueue.client.enums.ConsumeStatus;
import com.treefinance.delayqueue.common.ApplicationContextProvider;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author reveewu
 * @date 07/05/2018
 */
@Slf4j
public class ConsumeClient {
    private String groupName;
    private String topic;
    private Function<DelayMessageExt, ConsumeStatus> consumeHandler;
    private DelayQueueService delayQueueService;
    private ExecutorService executorService;

    public ConsumeClient(String groupName, String topic, Function<DelayMessageExt, ConsumeStatus> consumeHandler) {
        this.groupName = groupName;
        this.topic = topic;
        this.consumeHandler = consumeHandler;

        delayQueueService = ApplicationContextProvider.getBean(DelayQueueService.class);
    }

    public void start() {
        executorService = Executors.newSingleThreadExecutor(new DefaultThreadFactory(String.format("Delay-Queue-Consume-%s-%s-Thread", groupName, topic)));
        executorService.execute(() -> consume());

        // 添加应用关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Run shutdown hook now.");
            close();
        }, "DelayQueueConsumeShutdownHook"));
    }

    private void close() {
        try {
            executorService.shutdown();
        } catch (Exception e) {
            log.error("executorService error", e);
        }
    }

    private void consume() {
        while (!executorService.isShutdown()) {
            DelayMessageExt delayMessageExt;
            try {
                delayMessageExt = delayQueueService.pull(groupName, topic);
                if (null == delayMessageExt) {
                    return;
                }
            } catch (Exception e) {
                log.error("拉取延时队列消息出错, groupName: {}, topic: {} {}", groupName, topic, e);
                return;
            }

            try {
                ConsumeStatus consumeStatus = consumeHandler.apply(delayMessageExt);
                delayQueueService.callback(delayMessageExt, consumeStatus);

            } catch (Exception e) {
                log.error("消费延时队列消息出错, groupName: {}, topic: {} {}", groupName, topic, e);
                delayQueueService.callback(delayMessageExt, ConsumeStatus.RECONSUME);
            }
        }
    }
}
