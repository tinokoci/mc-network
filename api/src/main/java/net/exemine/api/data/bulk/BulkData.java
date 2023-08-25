package net.exemine.api.data.bulk;

import com.mongodb.client.model.Filters;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.data.stat.TimedStatSpan;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.model.PlayTime;
import net.exemine.api.permission.Permission;
import net.exemine.api.punishment.Punishment;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankInfo;
import net.exemine.api.redis.cache.model.DisguiseModel;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.DatabaseUtil;

import java.time.Period;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
@Setter
public class BulkData {

    @Getter(AccessLevel.NONE)
    private final UUID uuid;

    private final DatabaseService databaseService;
    private final BulkDataService bulkDataService;

    private final Set<RankInfo> rankInfoList = ConcurrentHashMap.newKeySet();
    private final Set<Punishment> punishmentList = ConcurrentHashMap.newKeySet();
    private final Set<Permission> permissionList = ConcurrentHashMap.newKeySet();
    private final Set<PlayTime> playTimeList = ConcurrentHashMap.newKeySet();

    private DisguiseModel disguiseModel;
    private TimeZone timeZone;

    public Rank getRank() {
        return getActiveRankInfoList()
                .stream()
                .map(RankInfo::getRank)
                .min(Comparator.comparingInt(Rank::getPriority))
                .orElse(Rank.DEFAULT);
    }

    public List<RankInfo> getActiveRankInfoList() {
        return rankInfoList
                .stream()
                .filter(RankInfo::isActive)
                .collect(Collectors.toList());
    }

    public List<Permission> getActivePermissionList() {
        return permissionList
                .stream()
                .filter(Permission::isActive)
                .collect(Collectors.toList());
    }

    public boolean hasPermission(String permission) {
        return getActivePermissionList()
                .stream()
                .anyMatch(p -> p.getNode().equalsIgnoreCase(permission));
    }

    public List<Punishment> getActivePunishments() {
        return punishmentList
                .stream()
                .filter(Punishment::isActive)
                .collect(Collectors.toList());
    }

    public List<Punishment> getActivePunishments(PunishmentType... types) {
        return punishmentList
                .stream()
                .filter(punishment -> punishment.isActive() && Arrays.stream(types).anyMatch(type -> type == punishment.getType()))
                .collect(Collectors.toList());
    }

    public Punishment getActivePunishment(PunishmentType... types) {
        return getActivePunishments()
                .stream()
                .filter(punishment -> List.of(types).contains(punishment.getType()))
                .findFirst()
                .orElse(null);
    }

    public Punishment getPunishmentByIndex(PunishmentType type, int index) {
        return getPunishmentsByType(type)
                .stream()
                .filter(punishment -> punishment.getIndex() == index)
                .findFirst()
                .orElse(null);
    }

    public Punishment getPunishmentById(String id) {
        return getPunishmentList()
                .stream()
                .filter(punishment -> punishment.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Punishment> getPunishmentsByType(PunishmentType type) {
        return punishmentList.stream().filter(punishment -> punishment.getType() == type)
                .sorted(Comparator.comparing(Punishment::isActive)
                        .thenComparing(Punishment::getAddedAt)
                        .reversed())
                .collect(Collectors.toList());
    }

    public List<Punishment> getPunishmentsByReason(PunishmentType type, String reason) {
        return getPunishmentsByType(type)
                .stream()
                .filter(punishment -> punishment.getAddedReason().equalsIgnoreCase(reason))
                .collect(Collectors.toList());
    }

    public boolean hasActivePunishment(PunishmentType... types) {
        return getActivePunishment(types) != null;
    }

    public long getPlayTime(TimedStatSpan timedStatSpan, long sessionLoginTime, InstanceType type) {
        return playTimeList
                .stream()
                .mapToLong(playtime -> playtime.getInTimeSpan(timedStatSpan, type))
                .sum() + getSessionPlayTime(sessionLoginTime);
    }

    public long getPlayTime(TimedStatSpan timedStatSpan, long sessionLoginTime) {
        return getPlayTime(timedStatSpan, sessionLoginTime, null);
    }

    public long getPlayTime(Period period, long sessionLoginTime, InstanceType type) {
        long playtime = playTimeList
                .stream()
                .filter(pt -> pt.getPeriodTimestamp().equals(period))
                .map(pt -> pt.getTotal(type))
                .findFirst()
                .orElse(0L);
        if (sessionLoginTime != 0L) {
            playtime += getSessionPlayTime(sessionLoginTime);
        }
        return playtime;
    }

    public long getPlayTime(Period period, long sessionLoginTime) {
        return getPlayTime(period, sessionLoginTime, null);
    }

    public long getPlayTime(long sessionLoginTime) {
        return playTimeList
                .stream()
                .mapToLong(PlayTime::getTotal)
                .sum() + getSessionPlayTime(sessionLoginTime);
    }

    public void updatePlayTime(long sessionLoginTime, InstanceType type) {
        if (playTimeList.isEmpty()) {
            bulkDataService.loadPlayTimes(this);
        }
        PlayTime playTime = getPlayTimeForCurrentDay();
        playTime.increase(type, getSessionPlayTime(sessionLoginTime));

        databaseService.update(
                DatabaseCollection.BULK_PLAYTIME,
                Filters.eq(DatabaseUtil.PRIMARY_KEY, playTime.getId()),
                GsonUtil.toDocument(playTime)
        ).run();
        playTimeList.clear();
    }

    private PlayTime getPlayTimeForCurrentDay() {
        return playTimeList
                .stream()
                .filter(pt -> pt.getPeriodTimestamp().equals(TimeUtil.getCurrentPeriod()))
                .findFirst()
                .orElse(new PlayTime(getUniqueId()));
    }

    private long getSessionPlayTime(long sessionLoginTime) {
        if (sessionLoginTime == 0L) return 0L;
        return System.currentTimeMillis() - sessionLoginTime;
    }

    public UUID getUniqueId() {
        return uuid;
    }
}