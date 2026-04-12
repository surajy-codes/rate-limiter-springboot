local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local refillIntervalSec = tonumber(ARGV[3])
local now = tonumber(ARGV[4])

local bucket = redis.call('HMGET', key, 'tokens', 'lastRefillTime')
local tokens = tonumber(bucket[1])
local lastRefillTime = tonumber(bucket[2])

if tokens == nil then
    tokens = capacity
    lastRefillTime = now
end

local elapsed = now - lastRefillTime
local tokensToAdd = math.floor(elapsed / refillIntervalSec) * refillRate
tokens = math.min(capacity, tokens + tokensToAdd)
lastRefillTime = lastRefillTime + math.floor(elapsed / refillIntervalSec) * refillIntervalSec

local allowed = 0
if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
end

redis.call('HMSET', key, 'tokens', tokens, 'lastRefillTime', lastRefillTime)
redis.call('EXPIRE', key, 3600)

return allowed