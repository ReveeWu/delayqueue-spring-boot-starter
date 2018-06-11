package com.treefinance.acrm.delayqueue.autoconfiguration;

import com.treefinance.acrm.delayqueue.core.DelayQueueProperties;
import com.treefinance.acrm.delayqueue.core.redis.RedisDelayQueue;
import com.treefinance.acrm.delayqueue.migrate.MigrateUtil;
import org.mvnsearch.spring.boot.redisson.RedissonAutoConfiguration;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * delayqueue auto configuration
 *
 * @author reveewu
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@EnableConfigurationProperties({DelayQueueProperties.class})
@AutoConfigureAfter({RedisAutoConfiguration.class, RedissonAutoConfiguration.class})
public class DelayQueueAutoConfiguration {
    @Bean
    public RedisDelayQueue redisDelayQueue(@Autowired DelayQueueProperties properties, @Autowired StringRedisTemplate redisTemplate, @Autowired RedissonClient redissonClient) {
        return new RedisDelayQueue(properties, redisTemplate, redissonClient);
    }

    @Bean
    public MigrateUtil migrateUtil(@Autowired DelayQueueProperties properties, @Autowired StringRedisTemplate redisTemplate) {
        return new MigrateUtil(properties, redisTemplate);
    }
}