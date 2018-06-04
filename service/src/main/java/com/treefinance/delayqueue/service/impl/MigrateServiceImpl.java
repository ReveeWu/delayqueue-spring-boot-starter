package com.treefinance.delayqueue.service.impl;

import com.alibaba.fastjson.JSON;
import com.treefinance.delayqueue.client.DelayQueueService;
import com.treefinance.delayqueue.client.bean.DelayMessage;
import com.treefinance.delayqueue.client.bean.DelayMessageExt;
import com.treefinance.delayqueue.client.enums.Status;
import com.treefinance.delayqueue.service.Constant;
import com.treefinance.delayqueue.service.MigrateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author reveewu
 * @date 31/05/2018
 */
@Slf4j
@Service
public class MigrateServiceImpl implements MigrateService {
    private DelayQueueService delayQueueService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 迁移
     *
     * @param key
     */
    @Override
    public void migrate(String key, String groupName, String topic) {
        log.info("迁移开始 key={}", key);

        try {
            final int pageSize = 10000;
            int page = 0;
            while (true) {
                Set<ZSetOperations.TypedTuple<String>> set = stringRedisTemplate.opsForZSet().rangeWithScores(key, page * pageSize, (page + 1) * pageSize - 1);
                log.info("start:{}, end:{}, size:{}", page * pageSize, (page + 1) * pageSize, set.size());
                if (CollectionUtils.isEmpty(set) || set.size() < pageSize) {
                    break;
                }

                stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    Iterator<ZSetOperations.TypedTuple<String>> iterable = set.iterator();
                    while (iterable.hasNext()) {
                        ZSetOperations.TypedTuple<String> item = iterable.next();
                        DelayMessage message = new DelayMessage(groupName, topic, item.getValue(), item.getValue(), new Date(item.getScore().longValue()));
                        DelayMessageExt messageExt = new DelayMessageExt();
                        messageExt.setGroupName(message.getGroupName());
                        messageExt.setTopic(message.getTopic());
                        messageExt.setDataKey(message.getDataKey());
                        messageExt.setBody(message.getBody());
                        messageExt.setDelay(message.getDelay());

                        messageExt.setId(String.format("%s:%s:%s", messageExt.getGroupName(), messageExt.getTopic(), messageExt.getDataKey()));
                        messageExt.setExecuteTime(messageExt.getCreateTime() + messageExt.getDelay());
                        messageExt.setStatus(Status.WAIT_PULL.name());


                        connection.hSet(Constant.META_DATA_KEY.getBytes(), messageExt.getId().getBytes(), JSON.toJSONString(messageExt).getBytes());
                        connection.zAdd(Constant.ZSET_KEY.getBytes(), item.getScore(), messageExt.getId().getBytes());
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
