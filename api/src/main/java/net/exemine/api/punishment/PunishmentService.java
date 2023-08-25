package net.exemine.api.punishment;

import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.redis.pubsub.model.PunishmentExecutionModel;
import net.exemine.api.redis.pubsub.model.generic.UUIDModel;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.callable.TypeCallback;
import net.exemine.api.util.string.DatabaseUtil;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PunishmentService {

    private final DatabaseService databaseService;
    private final RedisService redisService;

    public Punishment addPunishment(BulkData data, PunishmentType type, UUID issuer, long duration, String reason) {
        Punishment punishment = new Punishment(data.getPunishmentsByType(type).size() + 1, data.getUniqueId(), type, issuer, reason, duration);

        databaseService.insert(DatabaseCollection.BULK_PUNISHMENTS, GsonUtil.toDocument(punishment)).run();
        redisService.getPublisher().sendPunishmentExecution(data.getUniqueId(), punishment.getType(), punishment.getIndex());

        return punishment;
    }

    public void removePunishment(Punishment punishment, UUID issuer, String reason) {
        punishment.setActive(false);
        punishment.setRemovedAt(System.currentTimeMillis());
        punishment.setRemovedBy(issuer);
        punishment.setRemovedReason(reason.replace("-s", ""));
        punishment.setRemovedSilently(reason.contains("-s"));
        updatePunishment(punishment);
    }

    public void updatePunishment(Punishment punishment) {
        databaseService.update(
                DatabaseCollection.BULK_PUNISHMENTS,
                Filters.eq(DatabaseUtil.PRIMARY_KEY, punishment.getId()),
                GsonUtil.toDocument(punishment)
        ).run();
        redisService.getPublisher().sendPunishmentsUpdate(punishment.getUuid());
    }

    public Set<Punishment> fetchPunishments(UUID uuid) {
        return databaseService.findAll(DatabaseCollection.BULK_PUNISHMENTS, Filters.eq("uuid", uuid.toString()))
                .run()
                .stream()
                .map(document -> GsonUtil.fromJson(document.toJson(), Punishment.class))
                .collect(Collectors.toSet());
    }

    public void subscribeToPunishmentExecution(TypeCallback<PunishmentExecutionModel> punishmentExecutionCallback) {
        redisService.subscribe(RedisMessage.PUNISHMENT_EXECUTION, PunishmentExecutionModel.class, punishmentExecutionCallback);
    }

    public void subscribeToPunishmentUpdate(TypeCallback<UUIDModel> punishmentUpdateCallback) {
        redisService.subscribe(RedisMessage.PUNISHMENTS_UPDATE, UUIDModel.class, punishmentUpdateCallback);
    }
}
