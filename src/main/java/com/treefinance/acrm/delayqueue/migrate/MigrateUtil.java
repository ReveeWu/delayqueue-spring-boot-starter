package com.treefinance.acrm.delayqueue.migrate;

import com.alibaba.fastjson.JSON;
import com.treefinance.acrm.delayqueue.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author reveewu
 * @date 31/05/2018
 */
@Slf4j
public class MigrateUtil {
    private StringRedisTemplate stringRedisTemplate;
    private DelayQueueProperties properties;

    public MigrateUtil(DelayQueueProperties properties, StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.stringRedisTemplate = redisTemplate;
    }

    /**
     * 迁移
     *
     * @param key
     */
    public void migrate(String key, String topic) {
        log.info("迁移开始 key={}", key);

        try {
            final int pageSize = 10000;
            int page = 0;
            while (true) {
                Set<ZSetOperations.TypedTuple<String>> set = stringRedisTemplate.opsForZSet().rangeWithScores(key, page * pageSize, (page + 1) * pageSize - 1);
                log.info("start:{}, end:{}, size:{}", page * pageSize, (page + 1) * pageSize, set.size());
                if (CollectionUtils.isEmpty(set)) {
                    break;
                }

                stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    Iterator<ZSetOperations.TypedTuple<String>> iterable = set.iterator();
                    while (iterable.hasNext()) {
                        ZSetOperations.TypedTuple<String> item = iterable.next();
                        DelayMessage message = new DelayMessage(topic, item.getValue(), item.getValue(), new Date(item.getScore().longValue()));
                        DelayMessageExt messageExt = new DelayMessageExt();
                        messageExt.setGroupName(properties.getGroupName());
                        messageExt.setTopic(message.getTopic());
                        messageExt.setDataKey(message.getDataKey());
                        messageExt.setBody(message.getBody());
                        messageExt.setDelay(message.getDelay());

                        messageExt.setId(String.format("%s:%s:%s", messageExt.getGroupName(), messageExt.getTopic(), messageExt.getDataKey()));
                        messageExt.setExecuteTime(messageExt.getCreateTime() + messageExt.getDelay());
                        messageExt.setStatus(Status.WAIT_PULL.name());


                        connection.hSet(Constant.getMetaDataKey(properties.getGroupName()).getBytes(), messageExt.getId().getBytes(), JSON.toJSONString(messageExt).getBytes());
                        connection.zAdd(Constant.getZsetKey(properties.getGroupName()).getBytes(), item.getScore(), messageExt.getId().getBytes());
                    }

                    return null;
                });
                page++;
            }


        } catch (Exception e) {
            log.error("迁移出错", e);
        }
        log.info("迁移结束 key={}", key);
    }
}
