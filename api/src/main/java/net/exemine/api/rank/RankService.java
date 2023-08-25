package net.exemine.api.rank;

import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.link.LinkService;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.redis.pubsub.model.RankUpdateModel;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.callable.TypeCallback;
import net.exemine.api.util.string.DatabaseUtil;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RankService {

    private final DatabaseService databaseService;
    private final RedisService redisService;
    private LinkService linkService;

    public void init(LinkService linkService) {
        this.linkService = linkService;
    }

    public boolean addRank(CoreData coreData, BulkData bulkData, Rank rank, UUID issuer, long duration, String reason) {
        Set<RankInfo> rankInfoList = bulkData.getRankInfoList();
        Rank previousMainRank = bulkData.getRank();

        // Remove the same rank if present
        // Ranks can have different reasons / duration
        rankInfoList.stream()
                .filter(oldRankInfo -> oldRankInfo.isActive() && oldRankInfo.getRank() == rank)
                .forEach(oldRankInfo -> removeRank(coreData, bulkData, oldRankInfo, null));

        RankInfo rankInfo = new RankInfo(rankInfoList.size() + 1, bulkData.getUniqueId(), rank, issuer, reason, duration);
        rankInfoList.add(rankInfo);
        boolean hasMainRankChanged = rankInfo.getRank().isAbove(previousMainRank);

        databaseService.insert(DatabaseCollection.BULK_RANKS, GsonUtil.toDocument(rankInfo)).run();
        redisService.getPublisher().sendRankUpdate(bulkData.getUniqueId(), hasMainRankChanged);
        linkService.sendDiscordUpdate(coreData, bulkData);

        return hasMainRankChanged;
    }

    public boolean removeRank(CoreData coreData, BulkData bulkData, RankInfo rankInfoCopy, UUID issuer) {
        RankInfo rankInfo = bulkData.getRankInfoList().stream().filter(ri -> ri.getId().equals(rankInfoCopy.getId())).findFirst().orElse(null);
        if (rankInfo == null) return false;

        rankInfo.setRemoved(true);
        rankInfo.setRemovedBy(issuer);
        rankInfo.setRemovedAt(System.currentTimeMillis());

        boolean hasMainRankChanged = rankInfo.getRank().isAbove(bulkData.getRank());

        databaseService.update(DatabaseCollection.BULK_RANKS, Filters.eq(DatabaseUtil.PRIMARY_KEY, rankInfo.getId()), GsonUtil.toDocument(rankInfo)).run();
        redisService.getPublisher().sendRankUpdate(bulkData.getUniqueId(), hasMainRankChanged);
        linkService.sendDiscordUpdate(coreData, bulkData);

        return hasMainRankChanged;
    }

    public List<RankInfo> fetchRanks(UUID uuid) {
        return databaseService.findAll(DatabaseCollection.BULK_RANKS, Filters.eq("uuid", uuid.toString()))
                .run()
                .stream()
                .map(document -> GsonUtil.fromJson(document.toJson(), RankInfo.class))
                .collect(Collectors.toList());
    }

    public void subscribeToRankUpdate(TypeCallback<RankUpdateModel> rankUpdateCallback) {
        redisService.subscribe(RedisMessage.RANK_UPDATE, RankUpdateModel.class, rankUpdateCallback);
    }
}
