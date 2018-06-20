local zsetKey = KEYS[1]
local metaDataKey = KEYS[2]
local id = ARGV[1]
local metaData = ARGV[2]
local score = ARGV[3]
--如果不存在，保存元数据
local result = redis.call("hsetnx", metaDataKey, id, metaData)
if result==1 then
   --如果保存元数据成功，则添加zset数据
	result = redis.call("zadd", zsetKey, score, id)
end
return result