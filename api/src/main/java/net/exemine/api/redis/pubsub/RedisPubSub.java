package net.exemine.api.redis.pubsub;

import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import net.exemine.api.redis.RedisService;
import net.exemine.api.util.LogUtil;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;

@RequiredArgsConstructor
public class RedisPubSub extends JedisPubSub {

    private final RedisService redisService;

    @Override
    public void onMessage(String channel, String channelMessage) {
        if (!channel.equals(redisService.getChannel())) return;

        String[] args = channelMessage.split(redisService.getSplitChar());
        RedisMessage message = Arrays.stream(RedisMessage.values())
                .filter(redisMessage -> redisMessage.name().equals(args[0]))
                .findFirst()
                .orElse(null);
        if (message == null) return;

       RedisSubscriber<?> subscriber = redisService.getSubscribers().get(message.name());

       if (subscriber != null) {
           try {
               subscriber.execute(JsonParser.parseString(args[1]).getAsJsonObject());
           } catch (Exception e) {
               LogUtil.error("Caught an exception at redis pub-sub, manually handling it to avoid termination.");
               e.printStackTrace();
           }
       }
    }
}

