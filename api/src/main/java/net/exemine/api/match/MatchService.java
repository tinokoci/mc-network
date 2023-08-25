package net.exemine.api.match;

import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.ReflectionUtil;
import net.exemine.api.util.apache.RandomStringUtils;
import net.exemine.api.util.callable.TypeCallback;
import net.exemine.api.util.string.DatabaseUtil;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MatchService {

    private final DatabaseService databaseService;

    public <T extends Match> T createMatch(Class<T> clazz, TypeCallback<T> callback) {
        T match = ReflectionUtil.newInstance(clazz);
        match.setId(createMatchId());
        match.init();
        callback.run(match);
        databaseService.insert(match.getMongoCollection(), GsonUtil.toDocument(match)).run();
        return match;
    }

    public void updateMatch(Match match) {
        databaseService.update(
                match.getMongoCollection(),
                Filters.eq(DatabaseUtil.PRIMARY_KEY, match.getId()),
                GsonUtil.toDocument(match)
        ).run();
    }

    public <T extends Match> Optional<T> fetchMatch(Class<T> clazz, Bson bson) {
        Document document = databaseService.findOne(ReflectionUtil.newInstance(clazz).getMongoCollection(), bson).run();
        if (document == null) return Optional.empty();
        return Optional.of(GsonUtil.fromDocument(document, clazz));
    }

    public <T extends Match> List<T> getAllMatches(Class<T> clazz) {
        return databaseService.findAll(ReflectionUtil.newInstance(clazz).getMongoCollection())
                .run()
                .stream()
                .map(document -> GsonUtil.fromDocument(document, clazz))
                .collect(Collectors.toList());
    }

    private String createMatchId() {
        return RandomStringUtils.randomAlphanumeric(6);
    }
}
