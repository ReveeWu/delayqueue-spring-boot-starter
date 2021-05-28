package com.github.reveewu.delayqueue.autoconfiguration;

import com.github.reveewu.delayqueue.client.DelayQueueClient;
import com.github.reveewu.delayqueue.core.IDelayQueue;
import com.github.reveewu.delayqueue.core.redis.RedisDelayQueue;
import com.github.reveewu.delayqueue.migrate.MigrateUtil;
import com.github.reveewu.delayqueue.core.DelayQueueProperties;
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
@AutoConfigureAfter({RedisAutoConfiguration.class})
public class DelayQueueAutoConfiguration {
    @Bean
    public RedisDelayQueue redisDelayQueue(@Autowired DelayQueueProperties properties, @Autowired StringRedisTemplate redisTemplate) {
        return new RedisDelayQueue(properties, redisTemplate);
    }

    @Bean
    public DelayQueueClient delayQueueClient(@Autowired IDelayQueue delayQueue, @Autowired DelayQueueProperties properties) {
        return new DelayQueueClient(delayQueue, properties);
    }

    @Bean
    public MigrateUtil migrateUtil(@Autowired DelayQueueProperties properties, @Autowired StringRedisTemplate redisTemplate) {
        return new MigrateUtil(properties, redisTemplate);
    }
}