package com.treefinance.acrm.delayqueue.core.redis;

import com.alibaba.fastjson.JSON;
import com.treefinance.acrm.delayqueue.core.*;
import com.treefinance.acrm.delayqueue.core.exception.DataExistsException;
import com.treefinance.acrm.delayqueue.core.exception.DelayQueueException;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author reveewu
 * @date 07/06/2018
 */
@Slf4j
public class RedisDelayQueue implements IDelayQueue, DisposableBean, CommandLineRunner {
    private DelayQueueProperties properties;
    private StringRedisTemplate stringRedisTemplate;
    private RedissonClient redissonClient;

    private ExecutorService ready2PullExecutor = null;

    public RedisDelayQueue(DelayQueueProperties properties, StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.properties = properties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        try {
            ready2PullExecutor.shutdown();
        } catch (Exception e) {
            log.error("DelayQueueServiceShutdownHook error", e);
        }
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        if (null == ready2PullExecutor) {
            ready2PullExecutor = Executors.newSingleThreadExecutor(new DefaultThreadFactory("DelayQueue-MoveBrick-Thread"));
            ready2PullExecutor.execute(() -> moveBrickHandler());
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
        // 消息转换
        DelayMessageExt messageExt = new DelayMessageExt();
        messageExt.setGroupName(properties.getGroupName());
        messageExt.setTopic(message.getTopic());
        messageExt.setDataKey(message.getDataKey());
        messageExt.setBody(message.getBody());
        messageExt.setDelay(message.getDelay());

        messageExt.setId(String.format("%s:%s:%s", messageExt.getGroupName(), messageExt.getTopic(), messageExt.getDataKey()));
        messageExt.setExecuteTime(messageExt.getCreateTime() + messageExt.getDelay());
        messageExt.setStatus(Status.WAIT_PULL.name());

        Long re = stringRedisTemplate.execute(RedisLuaScripts.PUSH_SCRIPT,
                Arrays.asList(Constant.getZsetKey(properties.getGroupName()), Constant.getMetaDataKey(properties.getGroupName())),
                messageExt.getId(),
                JSON.toJSONString(messageExt),
                String.valueOf(messageExt.getExecuteTime()));

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
        String metaData = stringRedisTemplate.execute(RedisLuaScripts.PULL_SCRIPT, Arrays.asList(listKey, Constant.getMetaDataKey(properties.getGroupName())));
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
                stringRedisTemplate.opsForHash().delete(Constant.getMetaDataKey(properties.getGroupName()), messageExt.getId());
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
        RLock rLock = redissonClient.getLock(Constant.getMoveBrickLockName(properties.getGroupName()));

        while (!ready2PullExecutor.isShutdown()) {
            try {
                if (rLock.tryLock(60, 60, TimeUnit.SECONDS)) {
                    // 从zset取出
                    Set<String> set = stringRedisTemplate
                            .opsForZSet()
                            .rangeByScore(Constant.getZsetKey(properties.getGroupName()), 0, System.currentTimeMillis(), 0, 100);
                    if (CollectionUtils.isEmpty(set)) {
                        Thread.sleep(1000);
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("set size: {}", set.size());
                    }

                    // 根据id读取元数据
                    List<Object> metaDataKeys = new ArrayList<>(set);
                    List<Object> metaDataList = stringRedisTemplate.opsForHash().multiGet(Constant.getMetaDataKey(properties.getGroupName()), metaDataKeys);
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
                                stringRedisTemplate.opsForHash().put(Constant.getMetaDataKey(properties.getGroupName()), messageExt.getId(), JSON.toJSONString(messageExt));
                            });
                            // 删除zset
                            stringRedisTemplate.opsForZSet().remove(Constant.getZsetKey(properties.getGroupName()), set.toArray());

                            List<Object> re = stringRedisTemplate.exec();

                            return re;
                        }
                    });
                    if (log.isDebugEnabled()) {
                        log.debug("multi exec result: {}", JSON.toJSONString(re));
                    }
                }

            } catch (Exception e) {
                log.error("moveBrickHandler Error!", e);
            } finally {
                if (rLock.isLocked()) {
                    rLock.unlock();
                }
            }
        }
    }
}
