package net.exemine.api.cosmetic.tag;

import com.mongodb.client.model.Filters;
import lombok.AccessLevel;
import lombok.Getter;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.redis.RedisService;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.string.DatabaseUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TagService {

    private final DatabaseService databaseService;
    private final RedisService redisService;

    @Getter(AccessLevel.PACKAGE)
    private final Map<String, Tag> tags;

    public TagService(DatabaseService databaseService, RedisService redisService) {
        this.databaseService = databaseService;
        this.redisService = redisService;

        tags = databaseService.findAll(DatabaseCollection.TAGS)
                .run()
                .stream()
                .map(document -> GsonUtil.fromDocument(document, Tag.class))
                .collect(Collectors.toMap(Tag::getName, tag -> tag));

        new TagSubscriber(redisService, this);
    }

    public Tag getTag(String name) {
        return tags.get(name);
    }

    public void create(String name, String format) {
        Tag tag = new Tag(name, format);
        databaseService.insert(DatabaseCollection.TAGS, GsonUtil.toDocument(tag)).run();
        redisService.getPublisher().sendTagCreate(tag.getName(), tag.getFormat());
    }

    public void delete(Tag tag) {
        databaseService.delete(DatabaseCollection.TAGS, Filters.eq(DatabaseUtil.PRIMARY_KEY, tag.getName())).run();
        redisService.getPublisher().sendTagDelete(tag.getName());
    }

    public List<Tag> getAllTags() {
        return tags.values()
                .stream()
                .sorted(Comparator.comparing(Tag::getName))
                .collect(Collectors.toList());
    }
}
