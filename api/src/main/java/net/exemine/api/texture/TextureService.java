package net.exemine.api.texture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import net.exemine.api.controller.ApiController;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.util.CollectionUtil;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.string.DatabaseUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TextureService {

    private final DatabaseService databaseService;
    private final RedisService redisService;

    public final Map<String, TextureEntry> entries = new ConcurrentHashMap<>();

    public TextureService(DatabaseService databaseService, RedisService redisService) {
        this.databaseService = databaseService;
        this.redisService = redisService;
        refreshEntries();
        subscribeToTextureAdd();
    }

    @Nullable
    public TextureEntry getOrFetch(String name, boolean refreshIfOutdated) {
        ApiController.requireAsyncMinecraftThread();
        TextureEntry entry = get(name);

        if (entry != null && (!refreshIfOutdated || !entry.isTextureOutdated())) {
            return entry;
        }
        try {
            URL url0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader0 = new InputStreamReader(url0.openStream());
            JsonElement element = JsonParser.parseReader(reader0);
            if (!element.isJsonObject()) return null;

            String uuid = element.getAsJsonObject().get("id").getAsString();
            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader1 = new InputStreamReader(url2.openStream());
            JsonObject textureProperty = JsonParser.parseReader(reader1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String value = textureProperty.get("value").getAsString();
            String signature = textureProperty.get("signature").getAsString();

            entry = new TextureEntry(uuid, name, value, signature);
            redisService.getPublisher().sendTextureAdd(entry);
            databaseService.update(DatabaseCollection.SKIN_TEXTURES, Filters.eq(DatabaseUtil.PRIMARY_KEY, uuid), GsonUtil.toDocument(entry)).run();
            return entry;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public TextureEntry getOrFetch(String name) {
        return getOrFetch(name, true);
    }

    @Nullable
    public TextureEntry get(String name) {
        return entries.get(name.toLowerCase());
    }

    public void subscribeToTextureAdd() {
        redisService.subscribe(RedisMessage.TEXTURE_ADD, TextureEntry.class, entry -> entries.put(entry.getName().toLowerCase(), entry));
    }

    public void refreshEntries() {
        CollectionUtil.replace(entries, databaseService.findAll(DatabaseCollection.SKIN_TEXTURES)
                .run()
                .stream()
                .map(document -> GsonUtil.fromDocument(document, TextureEntry.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(entry -> entry.getName().toLowerCase(), entry -> entry)));
    }
}
