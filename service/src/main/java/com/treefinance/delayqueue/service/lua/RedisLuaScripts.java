package com.treefinance.delayqueue.service.lua;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * @author reveewu
 * @date 07/05/2018
 */
public class RedisLuaScripts {
    /**
     * push脚本
     */
    public static final RedisScript<Long> PUSH_SCRIPT = new DefaultRedisScript<>(new StringBuilder()
            .append("local zsetKey = KEYS[1]\n")
            .append("local metaDataKey = KEYS[2]\n")
            .append("local id = ARGV[1]\n")
            .append("local metaData = ARGV[2]\n")
            .append("local score = ARGV[3]\n")
            .append("--如果不存在，保存元数据\n")
            .append("local result = redis.call(\"hsetnx\", metaDataKey, id, metaData)\n")
            .append("if result==1 then\n")
            .append("   --如果保存元数据成功，则添加zset数据\n")
            .append("	result = redis.call(\"zadd\", zsetKey, score, id)\n")
            .append("end\n")
            .append("return result")
            .toString(), Long.class);


    /**
     * pull脚本
     */
    public static final RedisScript<String> PULL_SCRIPT = new DefaultRedisScript<>(new StringBuilder()
            .append("local listKey = KEYS[1]\n")
            .append("local metaDataKey = KEYS[2]\n")
            .append("--读取list最左边一条数据\n")
            .append("local dataId = redis.call(\"lpop\", listKey)\n")
            .append("if dataId then\n")
            .append("   --读取元数据\n")
            .append("   local metaData = redis.call(\"hget\", metaDataKey, dataId)\n")
            .append("   local metaDataJson = cjson.decode(metaData)\n")
            .append("   metaDataJson.Status=\"CONSUMING\"\n")
            .append("   metaDataJson = cjson.encode(metaDataJson)\n")
            .append("   return metaDataJson\n")
            .append("end\n")
            .append("return nil")
            .toString(), String.class);


}
