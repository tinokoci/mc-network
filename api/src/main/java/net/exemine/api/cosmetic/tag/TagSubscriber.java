package net.exemine.api.cosmetic.tag;

import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.redis.pubsub.model.TagCreateModel;
import net.exemine.api.redis.pubsub.model.generic.StringModel;

public class TagSubscriber {

    private final RedisService redisService;
    private final TagService tagService;

    public TagSubscriber(RedisService redisService, TagService tagService) {
        this.redisService = redisService;
        this.tagService = tagService;
        subscribeToTagCreate();
        subscribeToTagDelete();
    }

    private void subscribeToTagCreate() {
        redisService.subscribe(RedisMessage.TAG_CREATE, TagCreateModel.class, model -> {
            Tag tag = new Tag(model.getName(), model.getFormat());
            tagService.getTags().put(tag.getName(), tag);
        });
    }

    private void subscribeToTagDelete() {
        redisService.subscribe(RedisMessage.TAG_DELETE, StringModel.class,
                model -> tagService.getTags().remove(model.getMessage())
        );
    }
}
