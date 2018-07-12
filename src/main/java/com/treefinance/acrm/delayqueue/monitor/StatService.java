package com.treefinance.acrm.delayqueue.monitor;

import com.treefinance.acrm.delayqueue.core.Constant;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    public Map<StatKey, Integer> stat() {
        Stat stat = new Stat();

        String zsetKey = Constant.getZsetKey(properties.getGroupName());
        Cursor<ZSetOperations.TypedTuple<String>> cursor = stringRedisTemplate.opsForZSet().scan(zsetKey, ScanOptions.NONE);
        while (cursor.hasNext()) {
            ZSetOperations.TypedTuple<String> item = cursor.next();
            StatKey key = new StatKey(item.getValue(), item.getScore());
            stat.add(key);
        }

        return stat.getStatMap();
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
