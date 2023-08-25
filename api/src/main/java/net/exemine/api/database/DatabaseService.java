package net.exemine.api.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import net.exemine.api.controller.ApiController;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.string.Lang;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DatabaseService {

    private final MongoDatabase database;
    private final Map<DatabaseCollection, MongoCollection<Document>> collections = new ConcurrentHashMap<>();

    // False positive warning because we need to preserve the connection
    @SuppressWarnings("resource")
    public DatabaseService(String uri) {
        MongoClient client = new MongoClient(new MongoClientURI(uri));
        database = client.getDatabase(Lang.SERVER_NAME.toLowerCase() + '-' + ApiController.getInstance().getEnvironment());
    }

    public DatabaseAction<Void> insert(DatabaseCollection collection, Document document) {
        return createAction(() -> {
            getCollection(collection).insertOne(document);
            return null;
        });
    }

    public DatabaseAction<UpdateResult> update(DatabaseCollection collection, Bson query, Document document) {
        return createAction(() -> getCollection(collection).replaceOne(query, document, new ReplaceOptions().upsert(true)));
    }

    public DatabaseAction<DeleteResult> delete(DatabaseCollection collection, Bson query) {
        return createAction(() -> getCollection(collection).deleteOne(query));
    }

    public DatabaseAction<Document> findOne(DatabaseCollection collection, Bson query) {
        return createAction(() -> getCollection(collection).find(query).first());
    }

    public DatabaseAction<List<Document>> findAll(DatabaseCollection collection, Bson query) {
        return createAction(() -> getCollection(collection).find(query).into(new ArrayList<>()));
    }

    public DatabaseAction<List<Document>> findAll(DatabaseCollection collection) {
        return createAction(() -> getCollection(collection).find().into(new ArrayList<>()));
    }

    public <T> DatabaseAction<List<T>> findAll(DatabaseCollection collection, Class<T> clazz) {
        return createAction(() -> getCollection(collection).find().into(new ArrayList<>())
                .stream()
                .map(document -> GsonUtil.fromDocument(document, clazz))
                .collect(Collectors.toList()));
    }

    public <T> DatabaseAction<List<T>> findAllIntoList(DatabaseCollection collection, String key, Class<T> classOfKeyValues) {
        return createAction(() -> getCollection(collection).find().into(new ArrayList<>())
                .stream()
                .map(document -> document.get(key, classOfKeyValues))
                .collect(Collectors.toList()));
    }

    public DatabaseAction<Void> drop() {
        return createAction(() -> {
            database.drop();
            return null;
        });
    }

    private MongoCollection<Document> getCollection(DatabaseCollection databaseCollection) {
        MongoCollection<Document> mongoCollection = collections.get(databaseCollection);

        if (mongoCollection == null) {
            mongoCollection = database.getCollection(databaseCollection.toString());
            collections.put(databaseCollection, mongoCollection);
        }
        return mongoCollection;
    }

    private <T> DatabaseAction<T> createAction(Supplier<T> callback) {
        return new DatabaseAction<>(callback);
    }
}
