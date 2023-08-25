package net.exemine.api.data;

import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.template.DataTemplate;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.ReflectionUtil;
import net.exemine.api.util.string.DatabaseUtil;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DataService {

    private final DatabaseService databaseService;

    private final Map<Class<?>, DataTemplate> templates = new HashMap<>();

    public <T extends ExeData> Optional<T> fetch(Class<T> clazz, UUID uuid) {
        return fetch(clazz, DatabaseUtil.PRIMARY_KEY, uuid.toString());
    }

    public <T extends ExeData> Optional<T> fetch(Class<T> clazz, String name) {
        return fetch(clazz, DatabaseUtil.QUERY_NAME_KEY, name.toLowerCase());
    }

    public <T extends ExeData> Optional<T> fetch(Class<T> clazz, String key, String value) {
        Document document = databaseService.findOne(getTemplate(clazz).getCollection(), Filters.eq(key, value)).run();

        if (document == null) {
            return Optional.empty();
        }
        return Optional.of(GsonUtil.fromDocument(document, clazz));
    }

    public <T extends ExeData> List<T> fetchAll(Class<T> clazz) {
        return databaseService.findAll(getTemplate(clazz).getCollection())
                .run()
                .stream()
                .map(document -> GsonUtil.fromDocument(document, clazz))
                .collect(Collectors.toList());
    }

    public <T extends ExeData> void delete(Class<T> clazz, UUID uuid) {
        databaseService.delete(getTemplate(clazz).getCollection(), Filters.eq(DatabaseUtil.PRIMARY_KEY, uuid.toString())).run();
    }

    public <T extends ExeData> void update(T data) {
        databaseService.update(
                data.getMongoCollection(),
                Filters.eq(DatabaseUtil.PRIMARY_KEY, data.getUniqueId().toString()),
                GsonUtil.toDocument(data)
        ).run();
    }

    public <T extends ExeData> DataTemplate getTemplate(Class<T> clazz) {
        DataTemplate template = templates.get(clazz);

        if (template == null) {
            T data = ReflectionUtil.newInstance(clazz);
            template = new DataTemplate(GsonUtil.toDocument(data), data.getMongoCollection());
            templates.put(clazz, template);
        }
        return template;
    }
}
