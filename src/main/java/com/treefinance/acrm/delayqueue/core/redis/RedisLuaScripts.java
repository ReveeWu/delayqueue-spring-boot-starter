package com.treefinance.acrm.delayqueue.core.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.InputStream;

/**
 * @author reveewu
 * @date 07/05/2018
 */
@Slf4j
public class RedisLuaScripts {
    /**
     * push脚本
     */
    public static final RedisScript<Long> PUSH_SCRIPT = new DefaultRedisScript<>(readLuaScript("lua/push.lua"), Long.class);


    /**
     * pull脚本
     */
    public static final RedisScript<String> PULL_SCRIPT = new DefaultRedisScript<>(readLuaScript("lua/pull.lua"), String.class);

    /**
     * 搬砖脚本
     */
    public static final RedisScript<Long> MOVE_BRICK_SCRIPT = new DefaultRedisScript<>(readLuaScript("lua/moveBrick.lua"), Long.class);

    /**
     * 回收消费超时脚本
     */
    public static final RedisScript<String> RECYCLE_SCRIPT = new DefaultRedisScript<>(readLuaScript("lua/recycle.lua"), String.class);


    private static String readLuaScript(String path) {
        try {
            InputStream inputStream = RedisLuaScripts.class.getClassLoader().getResourceAsStream(path);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return new String(bytes);
        } catch (Exception e) {
            throw new RuntimeException("读取lua脚本出错," + path, e);
        }
    }
}
