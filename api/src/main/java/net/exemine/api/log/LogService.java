package net.exemine.api.log;

import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.DataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.instance.Instance;
import net.exemine.api.log.minecraft.MinecraftLog;
import net.exemine.api.log.minecraft.MinecraftLogType;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.Hastebin;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.DatabaseUtil;
import org.bson.conversions.Bson;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class LogService {

    private final DataService dataService;
    private final DatabaseService databaseService;

    public void insertLog(Log log) {
        databaseService.insert(log.getLogType().getCollection(), GsonUtil.toDocument(log)).run();
    }

    public void insertMinecraftLog(MinecraftLogType minecraftLogType, UUID uuid, boolean disguised, Instance instance, String displayName, String text) {
        String message = '[' + TimeUtil.getFullDate(System.currentTimeMillis()) + "] "
                + displayName
                + (disguised ? " [D]" : "")
                + text;
        MinecraftLog log = new MinecraftLog(minecraftLogType, message, uuid, instance.getName());
        insertLog(log);
    }

    /***
     * @return Hastebin URL
     */
    public String fetchMinecraftLogs(CoreData data, MinecraftLogType logType, long inclusiveStartTimestamp, long inclusiveEndTimestamp) {
        String message = databaseService.findAll(DatabaseCollection.LOGS_CHAT_MINECRAFT, Filters.eq(DatabaseUtil.UUID_KEY, data.getUniqueId().toString()))
                .run()
                .stream()
                .map(document -> GsonUtil.fromDocument(document, MinecraftLog.class))
                .filter(log -> log.getMinecraftLogType() == logType && TimeUtil.isInTimeSpan(log.getTimestamp(), inclusiveStartTimestamp, inclusiveEndTimestamp))
                .map(Log::getMessage)
                .collect(Collectors.joining("\n"));
        return Hastebin.paste(message, true);
    }

    /***
     * @return Hastebin URL
     */
    public String fetchMinecraftLogs(String name, MinecraftLogType logType, long inclusiveStartTimestamp, long inclusiveEndTimestamp) {
        AtomicReference<String> hastebinUrl = new AtomicReference<>("User data not found");
        dataService.fetch(CoreData.class, name).ifPresent(
                coreData -> hastebinUrl.set(fetchMinecraftLogs(coreData, logType, inclusiveStartTimestamp, inclusiveEndTimestamp))
        );
        return hastebinUrl.get();
    }

    public String fetchLogs(LogType type, Bson query, long inclusiveStartTimestamp, long inclusiveEndTimestamp) {
        String message = databaseService.findAll(type.getCollection(), query)
                .run()
                .stream()
                .map(document -> GsonUtil.fromDocument(document, MinecraftLog.class))
                .filter(log -> TimeUtil.isInTimeSpan(log.getTimestamp(), inclusiveStartTimestamp, inclusiveEndTimestamp))
                .map(Log::getMessage)
                .collect(Collectors.joining("\n"));
        return Hastebin.paste(message, true);
    }
}
