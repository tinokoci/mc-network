package net.exemine.api.leaderboard;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.exemine.api.data.DataService;
import net.exemine.api.data.ExeData;
import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.data.stat.TimedStatSpan;
import net.exemine.api.data.stat.number.impl.IntTimedStat;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.CollectionUtil;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.DatabaseUtil;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LeaderboardService<T extends ExeData> {

    private final Class<T> dataType;
    private final DataService dataService;
    private final BulkDataService bulkDataService;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, List<LeaderboardData>> leaderboards = new HashMap<>();
    private final Map<String, List<RatioLeaderboardData>> ratioLeaderboards = new HashMap<>(); // These kind of leaderboards consist out of two int stat values which are put in ratio
    private final List<T> data = new ArrayList<>();

    private LoadingCache<UUID, String> nameColorCache;

    private final long intervalInMillis = 5000L;

    public LeaderboardService(Class<T> dataType, DataService dataService, BulkDataService bulkDataService) {
        this.dataType = dataType;
        this.dataService = dataService;
        this.bulkDataService = bulkDataService;
        setupCache();
        update(); // update immediately, so we can feed info to other services. it's fine if it's sync since this is run on app start
        schedule();
    }

    public void setupCache() {
        nameColorCache = CacheBuilder.newBuilder()
                .expireAfterWrite(intervalInMillis * 2, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<UUID, String>() {
                    @Override
                    public @NotNull String load(@NotNull UUID uuid) {
                        return bulkDataService.getOrCreate(uuid, bulkDataService::loadRanks)
                                .getRank()
                                .getColor();
                    }
                });
    }

    public void update() {
        CollectionUtil.replace(data, dataService.fetchAll(dataType));
        List<Document> documents = data
                .stream()
                .map(GsonUtil::toDocument)
                .collect(Collectors.toList());
        if (documents.isEmpty()) return;

        Document template = GsonUtil.toDocument(dataService.getTemplate(dataType).getDocument());

        template.forEach((key, value) -> {
            List<LeaderboardData> dataList = documents
                    .stream()
                    .map(document -> {
                        UUID uuid = UUID.fromString(document.getString(DatabaseUtil.PRIMARY_KEY));
                        String name = document.getString("name");
                        String formattedName = getNameColor(uuid) + name;
                        IntTimedStat intTimedStat = getStatFromDocument(key, template, document);

                        return new LeaderboardData(uuid, name, formattedName, intTimedStat);
                    }).collect(Collectors.toList());

            leaderboards.put(key, dataList);
        });

        // Manually load ratio leaderboards
        ratioLeaderboards.put("kdr", documents.stream().map(document -> {
            UUID uuid = UUID.fromString(document.getString(DatabaseUtil.PRIMARY_KEY));
            String name = document.getString("name");
            String formattedName = getNameColor(uuid) + name;

            return new RatioLeaderboardData(uuid, name, formattedName,
                    getStatFromDocument("kills", template, document),
                    getStatFromDocument("deaths", template, document));
        }).collect(Collectors.toList()));
    }

    public void schedule() {
        executor.scheduleAtFixedRate(this::update, 0L, 5L, TimeUnit.SECONDS);
    }

    public List<LeaderboardData> getByKey(String key, TimedStatSpan timedStatSpan) {
        List<LeaderboardData> dataList = leaderboards.get(key);
        if (dataList == null) return Collections.emptyList();
        int limit = 10;

        AtomicInteger placing = new AtomicInteger(1);

        List<LeaderboardData> leaderboardDataList = dataList
                .stream()
                .sorted(Comparator.comparing(data -> data.getStat().getInTimeSpan(timedStatSpan), Comparator.reverseOrder()))
                .limit(limit)
                .map(data -> data
                        .setFormattedValue(StringUtil.formatNumber(data.getStat().getInTimeSpan(timedStatSpan)))
                        .setPlacing(placing.getAndIncrement()))
                .collect(Collectors.toList());

        if (leaderboardDataList.size() < limit) {
            IntStream.range(leaderboardDataList.size(), limit)
                    .mapToObj(i -> new LeaderboardData(null, "Unknown", Rank.DEFAULT.getColor() + "Unknown", new IntTimedStat(), "0", placing.getAndIncrement()))
                    .forEach(leaderboardDataList::add);
        }
        return leaderboardDataList;
    }

    public List<RatioLeaderboardData> getByKeyRatio(String key, TimedStatSpan timedStatSpan) {
        List<RatioLeaderboardData> dataList = ratioLeaderboards.get(key);
        if (dataList == null) return Collections.emptyList();
        int limit = 10;

        AtomicInteger placing = new AtomicInteger(1);

        List<RatioLeaderboardData> leaderboardDataList = dataList
                .stream()
                .sorted(Comparator.comparing(data -> data.getStatA().getInTimeSpan(timedStatSpan) /
                        Math.max(1, data.getStatB().getInTimeSpan(timedStatSpan)), Comparator.reverseOrder()))
                .limit(limit)
                .map(data -> data
                        .setFormattedValue(StringUtil.formatRatio(data.getStatA().getInTimeSpan(timedStatSpan),
                                data.getStatB().getInTimeSpan(timedStatSpan)))
                        .setPlacing(placing.getAndIncrement()))
                .collect(Collectors.toList());

        if (leaderboardDataList.size() < limit) {
            IntStream.range(leaderboardDataList.size(), limit)
                    .mapToObj(i -> new RatioLeaderboardData(null, "Unknown", Rank.DEFAULT.getColor() + "Unknown", new IntTimedStat(), new IntTimedStat(), "0.00", placing.getAndIncrement()))
                    .forEach(leaderboardDataList::add);
        }
        return leaderboardDataList;
    }

    public int getPlacing(String key, TimedStatSpan timedStatSpan, UUID uuid) {
        List<LeaderboardData> dataList = leaderboards.get(key);
        if (uuid == null || dataList == null) return -1;

        LeaderboardData personalData = dataList
                .stream()
                .filter(data -> data.getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);
        if (personalData == null) return -1;

        int index = dataList
                .stream()
                .sorted(Comparator.comparing(data -> data.getStat().getInTimeSpan(timedStatSpan), Comparator.reverseOrder()))
                .collect(Collectors.toList())
                .indexOf(personalData);
        if (index != -1) index++;

        return index;
    }

    public int getPlacingRatio(String key, TimedStatSpan timedStatSpan, UUID uuid) {
        List<RatioLeaderboardData> dataList = ratioLeaderboards.get(key);
        if (uuid == null || dataList == null) return -1;

        RatioLeaderboardData personalData = dataList
                .stream()
                .filter(data -> data.getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);
        if (personalData == null) return -1;

        int index = dataList
                .stream()
                .sorted(Comparator.comparing(data -> data.getStatA().getInTimeSpan(timedStatSpan) /
                        Math.max(1, data.getStatB().getInTimeSpan(timedStatSpan)), Comparator.reverseOrder()))
                .collect(Collectors.toList())
                .indexOf(personalData);
        if (index != -1) index++;

        return index;
    }

    private IntTimedStat getStatFromDocument(String key, Document template, Document document) {
        IntTimedStat intTimedStat;
        if (template.get(key) instanceof Document) {
            intTimedStat = GsonUtil.fromDocument(document.get(key, Document.class), IntTimedStat.class);
        } else {
            intTimedStat = new IntTimedStat();

            if (document.get(key) instanceof Integer) {
                intTimedStat.add(document.getInteger(key));
            }
        }
        return intTimedStat;
    }

    private String getNameColor(UUID uuid) {
        try {
            return nameColorCache.get(uuid);
        } catch (ExecutionException e) {
            return "";
        }
    }
}
