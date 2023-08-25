package net.exemine.api.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.LongSerializationPolicy;
import net.exemine.api.redis.RedisJsonObject;
import org.bson.Document;

import java.lang.reflect.Type;

public class GsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .create();

    public static String toJson(Object src) {
        return GSON.toJson(src);
    }

    public static Document toDocument(Object src) {
        return Document.parse(toJson(src));
    }

    public static JsonObject toJsonObject(Object src) {
        return GSON.toJsonTree(src).getAsJsonObject();
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return GSON.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

    public static <T> T fromJsonObject(JsonElement object, Type typeOfT) {
        try {
            return GSON.fromJson(object, typeOfT);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static <T> T fromJsonObject(RedisJsonObject object, Type typeOfT) {
        return GSON.fromJson(object.asJsonElement(), typeOfT);
    }

    public static <T> T fromDocument(Document document, Class<T> classOfT) {
        return fromJson(document.toJson(), classOfT);
    }

    public static <T> T fromDocument(Document document, Type typeOfT) {
        return fromJson(document.toJson(), typeOfT);
    }
}
