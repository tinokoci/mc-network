package net.exemine.api.redis;

import lombok.Getter;
import net.exemine.api.controller.ApiController;
import net.exemine.api.redis.cache.RedisCache;
import net.exemine.api.redis.cache.RedisKey;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.redis.pubsub.RedisPubSub;
import net.exemine.api.redis.pubsub.RedisPublisher;
import net.exemine.api.redis.pubsub.RedisSubscriber;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.callable.TypeCallback;
import net.exemine.api.util.string.Lang;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class RedisService {

    private final JedisPool subscriberPool, publisherPool;

    private final Map<String, RedisSubscriber<?>> subscribers = new HashMap<>();

    private final String splitChar = "<#>";
    private final String channel;

    @Getter
    private final RedisPublisher publisher;

    public RedisService(String uri) {
        subscriberPool = new JedisPool(uri);
        publisherPool = new JedisPool(uri);
        publisher = new RedisPublisher(this);
        channel = Lang.SERVER_NAME.toLowerCase() + '-' + ApiController.getInstance().getEnvironment();

        Executors.newSingleThreadExecutor().execute(() -> {
            try (Jedis jedis = subscriberPool.getResource()) {
                jedis.subscribe(new RedisPubSub(this), channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Pub-Sub
    public <T> void subscribe(RedisMessage message, Class<T> clazz, TypeCallback<T> callback) {
        subscribers.put(message.name(), new RedisSubscriber<>(clazz, callback));
    }

    public void publish(RedisMessage type, Object object) {
        tryWithResourceVoid(jedis -> jedis.publish(channel, type.name() + splitChar + GsonUtil.toJson(object)));
    }

    // Cache
    public <T> void addValueToHash(RedisCache cache, RedisKey key, T value) {
        tryWithResourceVoid(jedis -> jedis.hset(cache.name(), key.toString(), GsonUtil.toJson(value)));
    }

    public <T> void addValueToHash(RedisCache cache, UUID uuid, T value) {
        tryWithResourceVoid(jedis -> jedis.hset(cache.name(), uuid.toString(), GsonUtil.toJson(value)));
    }

    public <T> T getValueFromHash(RedisCache cache, RedisKey key, Class<T> clazz) {
        return tryWithResourceReturn(jedis -> GsonUtil.fromJson(jedis.hget(cache.name(), key.toString()), clazz));
    }

    public <T> T getValueFromHash(RedisCache cache, UUID key, Class<T> clazz) {
        return tryWithResourceReturn(jedis -> GsonUtil.fromJson(jedis.hget(cache.name(), key.toString()), clazz));
    }

    public void deleteValueFromHash(RedisCache cache, RedisKey key) {
        tryWithResourceVoid(jedis -> jedis.hdel(cache.name(), key.toString()));
    }

    public void deleteValueFromHash(RedisCache cache, UUID key) {
        tryWithResourceVoid(jedis -> jedis.hdel(cache.name(), key.toString()));
    }

    public Collection<String> getHashValues(RedisCache cache) {
        return tryWithResourceReturn(jedis -> jedis.hgetAll(cache.name()).values());
    }

    public List<String> getHashValues(RedisCache cache, RedisKey key) {
        return tryWithResourceReturn(jedis -> jedis.hgetAll(cache.name()).entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(key.toString()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList())
        );
    }

    public void deleteHash(RedisCache cache) {
        tryWithResourceVoid(jedis -> jedis.del(cache.name()));
    }

    public void setHash(RedisCache cache, Map<String, String> map) {
        tryWithResourceVoid(jedis -> jedis.hmset(cache.name(), map));
    }

    // Helper Functions
    private void tryWithResourceVoid(TypeCallback<Jedis> callback) {
        try (Jedis jedis = publisherPool.getResource()) {
            callback.run(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> T tryWithResourceReturn(Function<Jedis, T> callback) {
        try (Jedis jedis = publisherPool.getResource()) {
            return callback.apply(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
