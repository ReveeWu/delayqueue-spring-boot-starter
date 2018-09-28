local zsetKey = KEYS[1]
local metaDataKey = KEYS[2]
local id = ARGV[1]
local metaData = ARGV[2]
local score = ARGV[3]
local policy = ARGV[4]
local result = 0

if policy == "IGNORE" or policy == "ADD"
then
	--如果不存在，保存元数据
	result = redis.call("hsetnx", metaDataKey, id, metaData)
elseif policy == "COVER"
then
	--如果存在，覆盖元数据
	result = redis.call("hset", metaDataKey, id, metaData)
end

if result==1 or policy == "COVER" then
	--如果保存元数据成功，则添加zset数据
	result = redis.call("zadd", zsetKey, score, id)
end
return result
