local zsetKey = KEYS[1]
local metaDataKey = KEYS[2]
local listKeyFormat = KEYS[3]
local nowScore = ARGV[1]

--从zset取出100条已到期数据
local expireKeys = redis.call("zrangebyscore", zsetKey, "-inf", nowScore, "limit", 0, 100)

if expireKeys then
    for i,id in ipairs(expireKeys) do
        --读取元数据
        local metaData = redis.call("hget", metaDataKey, id)
        if metaData then
            local metaDataJson = cjson.decode(metaData)
            --拼接list key
            local listKey = listKeyFormat .. metaDataJson.topic
            --插入list末尾
            redis.call("rpush", listKey, id)
        end
        --删除zset
        redis.call("zrem", zsetKey, id)
    end
end

--返回条数
return table.getn(expireKeys)