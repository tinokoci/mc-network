package net.exemine.api.link;

import lombok.RequiredArgsConstructor;
import net.exemine.api.data.DataService;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankInfo;
import net.exemine.api.redis.RedisService;
import net.exemine.api.util.string.DatabaseUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class LinkService {

    private final DataService dataService;
    private final BulkDataService bulkDataService;
    private final RedisService redisService;

    public void linkAccount(CoreData coreData, String discordUserId) {
        coreData.setDiscordUserId(discordUserId);
        dataService.update(coreData);
        redisService.getPublisher().sendCoreDataUpdate(coreData.getUniqueId());

        BulkData bulkData = bulkDataService.getOrCreate(coreData.getUniqueId(), bulkDataService::loadRanks);
        sendDiscordUpdate(coreData, bulkData);
    }

    public void unlinkAccount(CoreData coreData) {
        String previousDiscordUserId = coreData.getDiscordUserId();

        coreData.setDiscordUserId(null);
        dataService.update(coreData);

        redisService.getPublisher().sendCoreDataUpdate(coreData.getUniqueId());
        redisService.getPublisher().sendDiscordUnlinkRequest(previousDiscordUserId);
    }

    public boolean sendDiscordUpdate(String discordUserId) {
        AtomicBoolean success = new AtomicBoolean();
        dataService.fetch(CoreData.class, DatabaseUtil.DISCORD_KEY, discordUserId).ifPresent(coreData -> {
            success.set(true);
            BulkData bulkData = bulkDataService.getOrCreate(coreData.getUniqueId(), bulkDataService::loadRanks);
            sendDiscordUpdate(coreData, bulkData);
        });
        return success.get();
    }

    public void sendDiscordUpdate(CoreData coreData, BulkData bulkData) {
        if (!coreData.isDiscordLinked()) return;
        List<Rank> ranks = bulkData.getActiveRankInfoList()
                .stream()
                .map(RankInfo::getRank)
                .collect(Collectors.toList());
        redisService.getPublisher().updateDiscordUser(coreData.getDiscordUserId(), coreData.getName(), ranks, coreData.isDiscordLocked());
    }
}
