package com.treefinance.delayqueue.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.treefinance.delayqueue.client.DelayQueueService;
import com.treefinance.delayqueue.client.bean.DelayMessage;
import com.treefinance.delayqueue.client.bean.DelayMessageExt;
import com.treefinance.delayqueue.client.enums.ConsumeStatus;
import com.treefinance.delayqueue.client.enums.Status;
import com.treefinance.delayqueue.common.ApplicationReadyListener;
import com.treefinance.delayqueue.service.Constant;
import com.treefinance.delayqueue.service.lua.RedisLuaScripts;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author reveewu
 * @date 04/05/2018
 */
@Slf4j
@Service(interfaceClass = DelayQueueService.class)
@Component
public class DelayQueueServiceImpl implements DelayQueueService, ApplicationReadyListener {
    private static final String LOCK_NAME = "DELAY_QUEUE_SERVICE_READY_LOCK";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    private ExecutorService ready2PullExecutor;

    /**
     * 应用启动成功时调用
     */
    @Override
    public void onApplicationReady() {
        ready2PullExecutor = Executors.newSingleThreadExecutor(new DefaultThreadFactory("Ready-To-Pull-Thread"));
        ready2PullExecutor.execute(() -> ready2PullHandler());

        // 添加应用关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Run shutdown hook now.");
            close();
        }, "DelayQueueServiceShutdownHook"));
    }

    private void close() {
        try {
            ready2PullExecutor.shutdown();
        } catch (Exception e) {
            log.error("DelayQueueServiceShutdownHook error", e);
        }
    }

    @Override
    public void push(DelayMessage message) {
        // 消息转换
        DelayMessageExt messageExt = new DelayMessageExt();
        messageExt.setGroupName(message.getGroupName());
        messageExt.setTopic(message.getTopic());
        messageExt.setDataKey(message.getDataKey());
        messageExt.setBody(message.getBody());
        messageExt.setDelay(message.getDelay());

        messageExt.setId(String.format("%s:%s:%s", messageExt.getGroupName(), messageExt.getTopic(), messageExt.getDataKey()));
        messageExt.setExecuteTime(messageExt.getCreateTime() + messageExt.getDelay());
        messageExt.setStatus(Status.WAIT_PULL.name());

        Long re = stringRedisTemplate.execute(RedisLuaScripts.PUSH_SCRIPT, Arrays.asList(Constant.ZSET_KEY, Constant.META_DATA_KEY), messageExt.getId(), JSON.toJSONString(messageExt), String.valueOf(messageExt.getExecuteTime()));
        if (log.isDebugEnabled()) {
            log.debug("push result: {}", re);
        }
    }

    @Override
    public DelayMessageExt pull(String groupName, String topic) {
        String listKey = Constant.getListKey(groupName, topic);
        String metaData = stringRedisTemplate.execute(RedisLuaScripts.PULL_SCRIPT, Arrays.asList(listKey, Constant.META_DATA_KEY));
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
                // 如果成功，删除元数据
                stringRedisTemplate.opsForHash().delete(Constant.META_DATA_KEY, messageExt.getId());
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
                        stringRedisTemplate.opsForHash().put(Constant.META_DATA_KEY, messageExt.getId(), JSON.toJSONString(messageExt));
                        stringRedisTemplate.opsForZSet().add(Constant.ZSET_KEY, messageExt.getId(), messageExt.getExecuteTime());
                        return stringRedisTemplate.exec();
                    }
                });

                break;
            default:
                break;
        }
    }

    private void ready2PullHandler() {
        RLock rLock = redissonClient.getLock(LOCK_NAME);

        while (!ready2PullExecutor.isShutdown()) {
            try {
                if(rLock.tryLock(60,60, TimeUnit.SECONDS)) {
                    // 从zset取出
                    Set<String> set = stringRedisTemplate
                            .opsForZSet()
                            .rangeByScore(Constant.ZSET_KEY, 0, System.currentTimeMillis(), 0, 100);
                    if (CollectionUtils.isEmpty(set)) {
                        Thread.sleep(1000);
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("set size: {}", set.size());
                    }

                    // 根据id读取元数据
                    List<Object> metaDataList = stringRedisTemplate.opsForHash().multiGet(Constant.META_DATA_KEY, Lists.newArrayList(set));
                    if (log.isDebugEnabled()) {
                        log.debug("metaDataList size: {}", metaDataList.size());
                    }

                    List<Object> re = stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
                        @Override
                        public <K, V> List<Object> execute(RedisOperations<K, V> operations) throws DataAccessException {
                            stringRedisTemplate.multi();

                            metaDataList.forEach(data -> {
                                DelayMessageExt messageExt = JSON.parseObject((String) data, DelayMessageExt.class);

                                // 存入相应topic属下的list
                                String key = Constant.getListKey(messageExt.getGroupName(), messageExt.getTopic());
                                stringRedisTemplate.opsForList()
                                        .rightPush(key, messageExt.getId());
                                // 更新元数据状态
                                messageExt.setStatus(Status.WAIT_CONSUME.name());
                                stringRedisTemplate.opsForHash().put(Constant.META_DATA_KEY, messageExt.getId(), JSON.toJSONString(messageExt));
                            });
                            // 删除zset
                            stringRedisTemplate.opsForZSet().remove(Constant.ZSET_KEY, set.toArray());

                            List<Object> re = stringRedisTemplate.exec();

                            return re;
                        }
                    });
                    if (log.isDebugEnabled()) {
                        log.debug("multi exec result: {}", JSON.toJSONString(re));
                    }
                }

            } catch (Exception e) {
                log.error("ready2PullHandler Error!", e);
            } finally {
                if (rLock.isLocked()) {
                    rLock.unlock();
                }
            }
        }
    }


}
