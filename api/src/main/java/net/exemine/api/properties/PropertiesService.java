package net.exemine.api.properties;

import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.string.DatabaseUtil;
import org.bson.Document;
import org.bson.conversions.Bson;

public class PropertiesService {

    private final DatabaseService databaseService;
    private final RedisService redisService;

    private final Bson query = Filters.eq(DatabaseUtil.PRIMARY_KEY, 0);

    @Getter
    private Properties properties = new Properties();

    public PropertiesService(DatabaseService databaseService, RedisService redisService) {
        this.databaseService = databaseService;
        this.redisService = redisService;

        refreshProperties();
        subscribeToPropertiesUpdate();
    }

    public void refreshProperties() {
        Document document = databaseService.findOne(DatabaseCollection.PROPERTIES, query)
                .ignoreThreadOverload() // run either on app start or async redis subscriber
                .run();
        if (document != null) {
            properties = GsonUtil.fromDocument(document, Properties.class);
        }
    }

    public void update() {
        databaseService.update(DatabaseCollection.PROPERTIES, query, GsonUtil.toDocument(properties)).run();
        redisService.getPublisher().sendPropertiesUpdate();
    }

    private void subscribeToPropertiesUpdate() {
        redisService.subscribe(RedisMessage.PROPERTIES_UPDATE, Object.class, model -> refreshProperties());
    }
}
