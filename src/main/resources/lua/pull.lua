local listKey = KEYS[1]
local metaDataKey = KEYS[2]
local consumingKey = KEYS[3]
local score = ARGV[1]
--读取list最左边一条数据
local dataId = redis.call("lpop", listKey)
if dataId then
   --读取元数据
   local metaData = redis.call("hget", metaDataKey, dataId)
   if nil ~= metaData then
      --添加到消费中zset
      redis.call("zadd", consumingKey, score, dataId)
   end
   --local metaDataJson = cjson.decode(metaData)
   --metaDataJson.Status="CONSUMING"
   --metaDataJson = cjson.encode(metaDataJson)
   return metaData
end
return nil