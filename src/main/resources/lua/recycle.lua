local zsetKey = KEYS[1]
local consumingKey = KEYS[2]
local timeoutScore = ARGV[1]
--读取消费超时列表
local set = redis.call("zrangebyscore", consumingKey, "-inf", timeoutScore)
for i,v in ipairs(set) do
   --添加到待消费zset
   local re = redis.call("zadd", zsetKey, timeoutScore, v)
   if re == 1 then
       --删除消费超时数据
       redis.call("zrem", consumingKey, v)
   end
end