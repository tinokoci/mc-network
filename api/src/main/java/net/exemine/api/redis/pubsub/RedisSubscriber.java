package net.exemine.api.redis.pubsub;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.callable.TypeCallback;

@RequiredArgsConstructor
public class RedisSubscriber<T> {

    private final Class<T> clazz;
    private final TypeCallback<T> callback;

    public void execute(JsonObject object) {
        if (callback != null) {
            callback.run(GsonUtil.fromJsonObject(object, clazz));
        }
    }
}
