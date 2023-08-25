package net.exemine.api.permission;

import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.redis.pubsub.model.generic.UUIDModel;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.callable.TypeCallback;
import net.exemine.api.util.string.DatabaseUtil;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PermissionService {

    private final DatabaseService databaseService;
    private final RedisService redisService;

    public void addPermission(BulkData data, String node, long duration) {
        Permission permission = data.getPermissionList()
                .stream()
                .filter(p -> p.getNode().equalsIgnoreCase(node))
                .findFirst()
                .orElse(new Permission(data.getUniqueId(), node.toLowerCase(), duration));

        permission.setAddedAt(System.currentTimeMillis());
        permission.setDuration(duration);
        permission.setRemoved(false);

        databaseService.update(DatabaseCollection.BULK_PERMISSIONS, Filters.eq(DatabaseUtil.PRIMARY_KEY, permission.getId()), GsonUtil.toDocument(permission)).run();
        redisService.getPublisher().sendPermissionsUpdate(data.getUniqueId());
    }

    public void removePermission(BulkData data, String node) {
        Permission permission = data.getPermissionList()
                .stream()
                .filter(p -> p.getNode().equalsIgnoreCase(node))
                .findFirst()
                .orElse(null);

        if (permission == null) return;
        permission.setRemoved(true);

        databaseService.update(DatabaseCollection.BULK_PERMISSIONS, Filters.eq(DatabaseUtil.PRIMARY_KEY, permission.getId()), GsonUtil.toDocument(permission)).run();
        redisService.getPublisher().sendPermissionsUpdate(data.getUniqueId());
    }

    public void clearPermissions(BulkData data) {
        data.getActivePermissionList().forEach(permission -> {
            permission.setRemoved(true);
            databaseService.update(DatabaseCollection.BULK_PERMISSIONS, Filters.eq(DatabaseUtil.PRIMARY_KEY, permission.getId()), GsonUtil.toDocument(permission)).run();
        });
        redisService.getPublisher().sendPermissionsUpdate(data.getUniqueId());
    }

    public Set<Permission> fetchPermissions(UUID uuid) {
        return databaseService.findAll(DatabaseCollection.BULK_PERMISSIONS, Filters.eq("uuid", uuid.toString()))
                .run()
                .stream()
                .map(document -> GsonUtil.fromJson(document.toJson(), Permission.class))
                .collect(Collectors.toSet());
    }

    public void subscribeToPermissionsUpdate(TypeCallback<UUIDModel> permissionsUpdateCallback) {
        redisService.subscribe(RedisMessage.PERMISSIONS_UPDATE, UUIDModel.class, permissionsUpdateCallback);
    }
}
