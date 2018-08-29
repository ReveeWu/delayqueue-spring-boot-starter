package com.treefinance.acrm.delayqueue.core.redis;

import com.alibaba.fastjson.JSON;
import com.treefinance.acrm.delayqueue.core.*;
import com.treefinance.acrm.delayqueue.core.exception.DataExistsException;
import com.treefinance.acrm.delayqueue.core.exception.DelayQueueException;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author reveewu
 * @date 07/06/2018
 */
@Slf4j
public class RedisDelayQueue implements IDelayQueue, DisposableBean, CommandLineRunner {
    private DelayQueueProperties properties;
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 搬砖线程
     * 把到期的数据从zset搬到topic对应的list中等待消费
     */
    private ExecutorService ready2PullExecutor = null;
    /**
     * 消费超时回收线程
     * 把消费中超过1分钟的数据回收到list重新消费
     */
    private ScheduledExecutorService consumingTimeoutRecycleExecutor = null;

    public RedisDelayQueue(DelayQueueProperties properties, StringRedisTemplate stringRedisTemplate) {
        this.properties = properties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void destroy() throws Exception {
        try {
            ready2PullExecutor.shutdown();
            consumingTimeoutRecycleExecutor.shutdown();
        } catch (Exception e) {
            log.error("DelayQueueServiceShutdownHook error", e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        if (null == ready2PullExecutor) {
            ready2PullExecutor = Executors.newSingleThreadExecutor(new DefaultThreadFactory("DelayQueue-MoveBrick-Thread"));
            ready2PullExecutor.execute(() -> moveBrickHandler());
        }
        if (null == consumingTimeoutRecycleExecutor) {
            consumingTimeoutRecycleExecutor = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory("DelayQueue-ConsumingTimeoutRecycle-Thread"));
            consumingTimeoutRecycleExecutor.scheduleAtFixedRate(() -> consumingTimeoutRecycleHandler(), 5, 60, TimeUnit.SECONDS);
        }
    }

    /**
     * 推送数据到延时队列
     *
     * @param message
     * @throws DataExistsException
     * @throws DelayQueueException
     */
    @Override
    public void push(DelayMessage message) throws DelayQueueException {
        push(message, PolicyEnum.IGNORE);
    }

    @Override
    public void push(DelayMessage message, PolicyEnum policy) throws DelayQueueException {
        // 消息转换
        DelayMessageExt messageExt = new DelayMessageExt();
        messageExt.setGroupName(properties.getGroupName());
        messageExt.setTopic(message.getTopic());
        messageExt.setDataKey(message.getDataKey());
        messageExt.setBody(message.getBody());
        messageExt.setDelay(message.getDelay());

        String messageExtId;
        if (PolicyEnum.ADD.equals(policy)) {
            messageExtId = String.format("%s:%s:%s:%s", messageExt.getGroupName(), messageExt.getTopic(), messageExt.getDataKey(), System.currentTimeMillis());
        } else {
            messageExtId = String.format("%s:%s:%s", messageExt.getGroupName(), messageExt.getTopic(), messageExt.getDataKey());
            if (null == policy) {
                policy = PolicyEnum.IGNORE;
            }
        }

        messageExt.setId(messageExtId);
        messageExt.setExecuteTime(messageExt.getCreateTime() + messageExt.getDelay());
        messageExt.setStatus(Status.WAIT_PULL.name());

        Long re = stringRedisTemplate.execute(RedisLuaScripts.PUSH_SCRIPT,
                Arrays.asList(Constant.getZsetKey(properties.getGroupName()), Constant.getMetaDataKey(properties.getGroupName())),
                messageExt.getId(),
                JSON.toJSONString(messageExt),
                String.valueOf(messageExt.getExecuteTime()),
                policy.name());

        if (log.isDebugEnabled()) {
            log.debug("push result: {}", re);
        }
    }

    /**
     * 拉取到期数据
     *
     * @param topic
     * @return
     */
    @Override
    public DelayMessageExt pull(String topic) throws DelayQueueException {
        String listKey = Constant.getListKey(properties.getGroupName(), topic);
        String consumingKey = Constant.getConsumingKey(properties.getGroupName());

        String metaData = stringRedisTemplate.execute(RedisLuaScripts.PULL_SCRIPT,
                Arrays.asList(listKey, Constant.getMetaDataKey(properties.getGroupName()), consumingKey),
                String.valueOf(System.currentTimeMillis()));
        if (StringUtils.isNotBlank(metaData)) {
            DelayMessageExt messageExt = JSON.parseObject(metaData, DelayMessageExt.class);
            return messageExt;
        }
        return null;
    }

    @Override
    public void callback(DelayMessageExt messageExt, ConsumeStatus consumeStatus) {
        switch (consumeStatus) {
            case SUCCESS:
            case FAIL:
                // 如果成功，删除元数据&消费中数据
                stringRedisTemplate.execute(new SessionCallback<Object>() {
                    @Override
                    public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                        stringRedisTemplate.multi();
                        stringRedisTemplate.opsForZSet().remove(Constant.getConsumingKey(properties.getGroupName()), messageExt.getId());
                        stringRedisTemplate.opsForHash().delete(Constant.getMetaDataKey(properties.getGroupName()), messageExt.getId());
                        return stringRedisTemplate.exec();
                    }
                });
                break;
            case RECONSUME:
                // 如果重新消费，重新塞入zset，并更新元数据状态
                messageExt.setExecuteTime(messageExt.getExecuteTime() + 1000 * 30);
                messageExt.setStatus(Status.WAIT_RECONSUME.name());
                messageExt.setConsumeErrorCnt(messageExt.getConsumeErrorCnt() + 1);

                stringRedisTemplate.execute(new SessionCallback<Object>() {
                    @Override
                    public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                        stringRedisTemplate.multi();
                        stringRedisTemplate.opsForHash().put(Constant.getMetaDataKey(properties.getGroupName()), messageExt.getId(), JSON.toJSONString(messageExt));
                        stringRedisTemplate.opsForZSet().add(Constant.getZsetKey(properties.getGroupName()), messageExt.getId(), messageExt.getExecuteTime());
                        return stringRedisTemplate.exec();
                    }
                });

                break;
            default:
                break;
        }
    }


    /**
     * 搬砖后台线程处理
     * 把到期的数据从zset搬到topic对应的list，等待消费
     */
    private void moveBrickHandler() {
        String zsetKey = Constant.getZsetKey(properties.getGroupName());
        String metaDataKey = Constant.getMetaDataKey(properties.getGroupName());
        String listKeyFormat = Constant.getListKey(properties.getGroupName(), "");

        while (!ready2PullExecutor.isShutdown()) {
            try {
                Long size = stringRedisTemplate.execute(RedisLuaScripts.MOVE_BRICK_SCRIPT,
                        Arrays.asList(zsetKey, metaDataKey, listKeyFormat),
                        String.valueOf(System.currentTimeMillis()));

                if (size == null || size <= 0) {
                    Thread.sleep(1000 * 1);
                } else if (log.isDebugEnabled()){
                    log.debug("move brick success size: {}", size);
                }
            } catch (Exception e) {
                log.error("moveBrickHandler Error!", e);
            }
        }
    }

    /**
     * 回收消费超时数据(2分钟)
     */
    private void consumingTimeoutRecycleHandler() {
        try {
            String zsetKey = Constant.getZsetKey(properties.getGroupName());
            String consumingKey = Constant.getConsumingKey(properties.getGroupName());

            stringRedisTemplate.execute(RedisLuaScripts.RECYCLE_SCRIPT,
                    Arrays.asList(zsetKey, consumingKey),
                    String.valueOf(System.currentTimeMillis() - (1000 * 120)));
        } catch (Exception e) {
            log.error("回收消费超时数据出错", e);
        }
    }
}
