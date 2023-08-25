package net.exemine.api.data.bulk;

import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.model.PlayTime;
import net.exemine.api.permission.PermissionService;
import net.exemine.api.punishment.Punishment;
import net.exemine.api.punishment.PunishmentService;
import net.exemine.api.rank.RankService;
import net.exemine.api.util.CollectionUtil;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.callable.TypeCallback;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BulkDataService {

    private final DatabaseService databaseService;
    private final PermissionService permissionService;
    private final PunishmentService punishmentService;
    private final RankService rankService;

    private final Map<UUID, BulkData> bulkDataMap = new ConcurrentHashMap<>();

    public BulkData getOrCreate(UUID uuid, TypeCallback<BulkData> success) {
        BulkData data = bulkDataMap.get(uuid);

        if (data == null) {
            data = new BulkData(uuid, databaseService, this);
            bulkDataMap.put(uuid, data);
        }
        success.run(data);
        return data;
    }

    public BulkData getOrCreate(UUID uuid) {
        return getOrCreate(uuid, TypeCallback.EMPTY());
    }

    public void loadData(BulkData data) {
        loadRanks(data);
        loadPunishments(data);
        loadPermissions(data);
    }

    public void loadRanks(BulkData data) {
        CollectionUtil.replace(
                data.getRankInfoList(),
                rankService.fetchRanks(data.getUniqueId())
                        .stream()
                        .filter(rankInfo -> rankInfo.getRank() != null)
                        .collect(Collectors.toSet())
        );
    }

    public void loadPunishments(BulkData data) {
        CollectionUtil.replace(data.getPunishmentList(), punishmentService.fetchPunishments(data.getUniqueId()));
        data.getActivePunishments()
                .stream()
                .filter(Punishment::isExpired)
                .forEach(punishment -> punishmentService.removePunishment(punishment, null, "Expired"));
    }

    public void loadPermissions(BulkData data) {
        CollectionUtil.replace(data.getPermissionList(), permissionService.fetchPermissions(data.getUniqueId()));
    }

    public void loadPlayTimes(BulkData data) {
        Set<PlayTime> playTimeSet = data.getPlayTimeList();
        if (!playTimeSet.isEmpty()) return;

        List<PlayTime> fetchedPlayTimeList = databaseService.findAll(DatabaseCollection.BULK_PLAYTIME, Filters.eq("uuid", data.getUniqueId().toString()))
                .run()
                .stream()
                .map(document -> GsonUtil.fromDocument(document, PlayTime.class))
                .collect(Collectors.toList());
        playTimeSet.addAll(fetchedPlayTimeList);
    }
}
