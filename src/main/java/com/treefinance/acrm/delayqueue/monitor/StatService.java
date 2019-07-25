package com.treefinance.acrm.delayqueue.monitor;

import com.alibaba.fastjson.JSON;
import com.treefinance.acrm.delayqueue.core.Constant;
import com.treefinance.acrm.delayqueue.core.DelayMessageExt;
import com.treefinance.acrm.delayqueue.core.DelayQueueProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author reveewu
 * @date 25/06/2018
 */
@Slf4j
@Component
public class StatService {
    @Autowired
    private DelayQueueProperties properties;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH");

    /**
     * 根据id 查询metadata
     * @param id
     * @return
     */
    public DelayMessageExt getMetadata(String id) {
        String redisKey =  Constant.getMetaDataKey(properties.getGroupName());
        Object metadata = stringRedisTemplate.opsForHash().get(redisKey, id);

        DelayMessageExt messageExt = JSON.parseObject(metadata.toString(), DelayMessageExt.class);

        return messageExt;
    }

    /**
     * 查询某时间点之前所有metadata
     * @param time
     * @return
     */
    public List<DelayMessageExt> listMetadata(Date time) {
        List<DelayMessageExt> list = new ArrayList<>(100);

        String zsetKey = Constant.getZsetKey(properties.getGroupName());
        Set<String> result = stringRedisTemplate.opsForZSet().rangeByScore(zsetKey, 0L, time.getTime());

        for(String item : result) {
            DelayMessageExt messageExt = JSON.parseObject(item, DelayMessageExt.class);
            list.add(messageExt);
        }

        return list;
    }

    /**
     * 统计某时间点前未消费zset数量
     * @param time
     * @return
     */
    public Long countZset(Date time) {
        String zsetKey = Constant.getZsetKey(properties.getGroupName());
        return stringRedisTemplate.opsForZSet().count(zsetKey, 0L, time.getTime());
    }

    /**
     * 根据topic统计list未消费数量
     * @param topic
     * @return
     */
    public Long countList(String topic) {
        String redisKey = Constant.getListKey(properties.getGroupName(), topic);
        return stringRedisTemplate.opsForList().size(redisKey);
    }

    /**
     * 根据topic统计consuming list数量
     * @return
     */
    public Long countConsuming() {
        String redisKey = Constant.getConsumingKey(properties.getGroupName());
        return stringRedisTemplate.opsForZSet().size(redisKey);
    }

    /**
     * 统计未消费zset时间分布(按小时)
     * @return
     */
    public Map<String, Integer> timeSpread() {
        Map<String, Integer> map = new HashMap<>(32);

        String zsetKey = Constant.getZsetKey(properties.getGroupName());
        Cursor<ZSetOperations.TypedTuple<String>> cursor = stringRedisTemplate.opsForZSet().scan(zsetKey, ScanOptions.NONE);
        while (cursor.hasNext()) {
            ZSetOperations.TypedTuple<String> item = cursor.next();

            String timeKey =  SIMPLE_DATE_FORMAT.format(new Date(item.getScore().longValue()));
            if (!map.containsKey(timeKey)) {
                map.put(timeKey, 1);
            } else {
                map.put(timeKey, map.get(timeKey) + 1);
            }

        }

        return map;
    }


    @Data
    public class StatKey {
        private String topic;
        private String month;

        public StatKey(String item, Double score) {
            String[] arr = item.split(":");
            this.topic = arr[1];
            this.month = DateFormatUtils.format(new Date(score.longValue()), "yyyy-MM");
        }

        @Override
        public String toString() {
            return String.format("%s:%s", topic, month);
        }

        @Override
        public boolean equals(Object obj) {
            return this.toString()==obj.toString();
        }
    }

    @Data
    public class Stat {
        private Map<StatKey, Integer> statMap = new HashMap<>();

        public void add(StatKey key) {
            if (!statMap.containsKey(key)) {
                statMap.put(key, 1);
            } else {
                statMap.put(key, statMap.get(key) + 1);
            }
        }
    }
}
