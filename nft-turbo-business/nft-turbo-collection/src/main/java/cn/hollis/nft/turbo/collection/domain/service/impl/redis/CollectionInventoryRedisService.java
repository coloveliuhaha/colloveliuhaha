package cn.hollis.nft.turbo.collection.domain.service.impl.redis;

import cn.hollis.nft.turbo.collection.domain.request.CollectionInventoryRequest;
import cn.hollis.nft.turbo.collection.domain.response.CollectionInventoryResponse;
import cn.hollis.nft.turbo.collection.domain.service.CollectionInventoryService;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.client.codec.IntegerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static cn.hollis.nft.turbo.base.response.ResponseCode.BIZ_ERROR;
import static cn.hollis.nft.turbo.base.response.ResponseCode.DUPLICATED;

/**
 * 藏品库存服务-基于Redis
 *
 * @author Hollis
 */
@Service
@Primary
public class CollectionInventoryRedisService implements CollectionInventoryService {

    private static final Logger logger = LoggerFactory.getLogger(CollectionInventoryRedisService.class);

    @Autowired
    private RedissonClient redissonClient;

    private static final String INVENTORY_KEY = "clc:inventory:";
    private static final String INVENTORY_STREAM_KEY = "clc:inventory:stream:";

    private static final String ERROR_CODE_INVENTORY_NOT_ENOUGH = "INVENTORY_NOT_ENOUGH";
    private static final String ERROR_CODE_KEY_NOT_FOUND = "KEY_NOT_FOUND";
    private static final String ERROR_CODE_OPERATION_ALREADY_EXECUTED = "OPERATION_ALREADY_EXECUTED";

    @Override
    public CollectionInventoryResponse init(CollectionInventoryRequest request) {
        CollectionInventoryResponse collectionInventoryResponse = new CollectionInventoryResponse();
        if (redissonClient.getBucket(getCacheKey(request)).isExists()) {
            collectionInventoryResponse.setSuccess(true);
            collectionInventoryResponse.setResponseCode(DUPLICATED.name());
            return collectionInventoryResponse;
        }
        redissonClient.getBucket(getCacheKey(request)).set(request.getInventory());
        collectionInventoryResponse.setSuccess(true);
        collectionInventoryResponse.setCollectionId(request.getCollectionId());
        collectionInventoryResponse.setIdentifier(request.getIdentifier());
        collectionInventoryResponse.setInventory(request.getInventory());
        return collectionInventoryResponse;
    }

    @Override
    public Integer getInventory(CollectionInventoryRequest request) {
        Integer stock = (Integer) redissonClient.getBucket(getCacheKey(request), IntegerCodec.INSTANCE).get();
        return stock;
    }

    @Override
    public CollectionInventoryResponse decrease(CollectionInventoryRequest request) {
        CollectionInventoryResponse collectionInventoryResponse = new CollectionInventoryResponse();
        String luaScript = """
                if redis.call('hexists', KEYS[2], ARGV[2]) == 1 then
                    return redis.error_reply('OPERATION_ALREADY_EXECUTED')
                end
                                
                local current = redis.call('get', KEYS[1])
                if current == false then
                    return redis.error_reply('KEY_NOT_FOUND')
                end
                if tonumber(current) == nil then
                    return redis.error_reply('current value is not a number')
                end
                if tonumber(current) < tonumber(ARGV[1]) then
                    return redis.error_reply('INVENTORY_NOT_ENOUGH')
                end
                                
                local new = tonumber(current) - tonumber(ARGV[1])
                redis.call('set', KEYS[1], tostring(new))
                                
                -- 获取Redis服务器的当前时间（秒和微秒）
                local time = redis.call("time")
                -- 转换为毫秒级时间戳
                local currentTimeMillis = (time[1] * 1000) + math.floor(time[2] / 1000)
                                
                -- 使用哈希结构存储日志
                redis.call('hset', KEYS[2], ARGV[2], cjson.encode({
                    action = "decrease",
                    from = current,
                    to = new,
                    change = ARGV[1],
                    by = ARGV[2],
                    timestamp = currentTimeMillis
                }))
                                
                return new
                """;

        try {
            Integer result = ((Long) redissonClient.getScript().eval(RScript.Mode.READ_WRITE,
                    luaScript,
                    RScript.ReturnType.INTEGER,
                    Arrays.asList(getCacheKey(request), getCacheStreamKey(request)),
                    request.getInventory(), request.getIdentifier())).intValue();

            collectionInventoryResponse.setSuccess(true);
            collectionInventoryResponse.setCollectionId(request.getCollectionId());
            collectionInventoryResponse.setIdentifier(request.getIdentifier());
            collectionInventoryResponse.setInventory(result);
            return collectionInventoryResponse;

        } catch (RedisException e) {
            logger.error("decrease error , collectionId = {} , identifier = {} ,", request.getCollectionId(), request.getIdentifier(), e);
            collectionInventoryResponse.setSuccess(false);
            collectionInventoryResponse.setCollectionId(request.getCollectionId());
            collectionInventoryResponse.setIdentifier(request.getIdentifier());
            if (e.getMessage().startsWith(ERROR_CODE_INVENTORY_NOT_ENOUGH)) {
                collectionInventoryResponse.setResponseCode(ERROR_CODE_INVENTORY_NOT_ENOUGH);
            } else if (e.getMessage().startsWith(ERROR_CODE_KEY_NOT_FOUND)) {
                collectionInventoryResponse.setResponseCode(ERROR_CODE_KEY_NOT_FOUND);
            } else if (e.getMessage().startsWith(ERROR_CODE_OPERATION_ALREADY_EXECUTED)) {
                collectionInventoryResponse.setResponseCode(ERROR_CODE_OPERATION_ALREADY_EXECUTED);
                collectionInventoryResponse.setSuccess(true);
            } else {
                collectionInventoryResponse.setResponseCode(BIZ_ERROR.name());
            }
            collectionInventoryResponse.setResponseMessage(e.getMessage());

            return collectionInventoryResponse;
        }
    }

    @Override
    public List<Object> getInventoryDecreaseLogs(CollectionInventoryRequest request) {
        return redissonClient.getScoredSortedSet(getCacheStreamKey(request)).stream().toList();
    }

    @Override
    public CollectionInventoryResponse increase(CollectionInventoryRequest request) {
        CollectionInventoryResponse collectionInventoryResponse = new CollectionInventoryResponse();
        String luaScript = """
                if redis.call('hexists', KEYS[2], ARGV[2]) == 1 then
                    return redis.error_reply('OPERATION_ALREADY_EXECUTED')
                end
                                
                local current = redis.call('get', KEYS[1])
                if current == false then
                    return redis.error_reply('key not found')
                end
                if tonumber(current) == nil then
                    return redis.error_reply('current value is not a number')
                end
                                
                local new = (current == nil and 0 or tonumber(current)) + tonumber(ARGV[1])
                redis.call('set', KEYS[1], tostring(new))
                                
                -- 获取Redis服务器的当前时间（秒和微秒）
                local time = redis.call("time")
                -- 转换为毫秒级时间戳
                local currentTimeMillis = (time[1] * 1000) + math.floor(time[2] / 1000)
                                
                -- 使用哈希结构存储日志
                redis.call('hset', KEYS[2], ARGV[2], cjson.encode({
                    action = "increase",
                    from = current,
                    to = new,
                    change = ARGV[1],
                    by = ARGV[2],
                    timestamp = currentTimeMillis
                }))
                                
                return new
                """;

        try {
            Integer result = ((Long) redissonClient.getScript().eval(RScript.Mode.READ_WRITE,
                    luaScript,
                    RScript.ReturnType.INTEGER,
                    Arrays.asList(getCacheKey(request), getCacheStreamKey(request)),
                    request.getInventory(), request.getIdentifier())).intValue();

            collectionInventoryResponse.setSuccess(true);
            collectionInventoryResponse.setCollectionId(request.getCollectionId());
            collectionInventoryResponse.setIdentifier(request.getIdentifier());
            collectionInventoryResponse.setInventory(result);
            return collectionInventoryResponse;

        } catch (RedisException e) {
            logger.error("increase error , collectionId = {} , identifier = {} ,", request.getCollectionId(), request.getIdentifier(), e);
            collectionInventoryResponse.setSuccess(false);
            collectionInventoryResponse.setCollectionId(request.getCollectionId());
            collectionInventoryResponse.setIdentifier(request.getIdentifier());
            if (e.getMessage().startsWith(ERROR_CODE_KEY_NOT_FOUND)) {
                collectionInventoryResponse.setResponseCode(ERROR_CODE_KEY_NOT_FOUND);
            } else if (e.getMessage().startsWith(ERROR_CODE_OPERATION_ALREADY_EXECUTED)) {
                collectionInventoryResponse.setResponseCode(ERROR_CODE_OPERATION_ALREADY_EXECUTED);
                collectionInventoryResponse.setSuccess(true);
            } else {
                collectionInventoryResponse.setResponseCode(BIZ_ERROR.name());
            }
            collectionInventoryResponse.setResponseMessage(e.getMessage());

            return collectionInventoryResponse;
        }
    }

    @Override
    public void invalid(CollectionInventoryRequest request) {
        if (redissonClient.getBucket(getCacheKey(request)).isExists()) {
            redissonClient.getBucket(getCacheKey(request)).delete();
        }

        if (redissonClient.getBucket(getCacheStreamKey(request)).isExists()) {
            // 让流水记录的过期时间设置为24小时后，这样可以避免流水记录立即过期，对账出现问题
            redissonClient.getBucket(getCacheStreamKey(request)).expire(Instant.now().plus(24, ChronoUnit.HOURS));
        }
    }

    @NotNull
    private static String getCacheKey(CollectionInventoryRequest request) {
        return INVENTORY_KEY + request.getCollectionId();
    }

    @NotNull
    private static String getCacheStreamKey(CollectionInventoryRequest request) {
        return INVENTORY_STREAM_KEY + request.getCollectionId();
    }
}
