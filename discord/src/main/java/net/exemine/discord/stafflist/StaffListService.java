package net.exemine.discord.stafflist;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.exemine.api.data.DataService;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.properties.Properties;
import net.exemine.api.properties.PropertiesService;
import net.exemine.api.proxy.ProxyCheck;
import net.exemine.api.proxy.ProxyService;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.Executor;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.string.DatabaseUtil;
import net.exemine.api.util.string.Lang;
import net.exemine.discord.Discord;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.MemberUtil;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StaffListService {

    private final DatabaseService databaseService;
    private final DataService dataService;
    private final BulkDataService bulkDataService;
    private final PropertiesService propertiesService;
    private final ProxyService proxyService;

    private final Map<UUID, StaffListData> staffListExtras = new HashMap<>();
    private final EnumSet<Rank> emptyIgnoredRanks = EnumSet.of(Rank.DEVELOPER);

    public StaffListService(Discord discord) {
        this.databaseService = discord.getDatabaseService();
        this.dataService = discord.getDataService();
        this.bulkDataService = discord.getBulkDataService();
        this.propertiesService = discord.getPropertiesService();
        this.proxyService = discord.getProxyService();

        databaseService.findAll(DatabaseCollection.STAFF_LIST)
                .run()
                .stream()
                .map(document -> GsonUtil.fromDocument(document, StaffListData.class))
                .forEach(staffListData -> staffListExtras.put(staffListData.getUniqueId(), staffListData));

        Executor.schedule(this::run).runAsyncTimer(0, 10_000L);
    }

    private void run() {
        Message message = getMessage();
        if (message == null) return;

        LinkedList<CoreData> coreDataList = dataService.fetchAll(CoreData.class)
                .stream()
                .sorted(Comparator.comparingInt(coreData -> getListPriority(coreData.getUniqueId())))
                .collect(Collectors.toCollection(LinkedList::new));
        LinkedList<BulkData> bulkDataList = coreDataList
                .stream()
                .map(coreData -> bulkDataService.getOrCreate(coreData.getUniqueId(), bulkDataService::loadRanks))
                .collect(Collectors.toCollection(LinkedList::new));

        StringBuilder builder = new StringBuilder();
        builder.append(DiscordUtil.boldUnderline("Official " + Lang.SERVER_NAME + " Staff Lists")).append(" ❤");
        builder.append("\n\n");

        Arrays.stream(Rank.values())
                .filter(rank -> rank.isEqualOrAbove(RankType.STAFF) && (!emptyIgnoredRanks.contains(rank) || hasMembers(bulkDataList, rank)))
                .forEach(rank -> {
                    builder.append(DiscordUtil.boldUnderline(rank.getDiscordRole()));

                    if (bulkDataList
                            .stream()
                            .noneMatch(bulkData -> bulkData.getRank().isEqual(rank))) {
                        builder.append("\nNone");
                    }
                    IntStream.range(0, bulkDataList.size()).forEach(i -> {
                        BulkData bulkData = bulkDataList.get(i);
                        if (!bulkData.getRank().isEqual(rank)) return;

                        CoreData coreData = coreDataList.get(i);
                        String discordUserId = coreData.getDiscordUserId();
                        Member member = discordUserId == null
                                ? null
                                : MemberUtil.getMember(discordUserId);
                        String user = member == null
                                ? coreData.getName()
                                : member.getAsMention();

                        Emoji headEmoji = DiscordUtil.getGuild().getEmojisByName(coreData.getName(), true)
                                .stream()
                                .findFirst()
                                .orElse(null);
                        boolean hasPosition = hasPosition(coreData.getUniqueId());
                        String separator = headEmoji != null || hasPosition ? " | " : "";
                        builder.append("\n - ").append(user);

                        ProxyCheck proxyCheck = proxyService.getOrCheckAddress(coreData.getAddress());
                        if (!proxyCheck.isUnknown()) {
                            Emoji flagEmoji = Emoji.fromFormatted(":flag_" + proxyCheck.getIsoCode().toLowerCase() + ':');
                            builder.append(" ").append(flagEmoji.getFormatted());
                        }
                        if (hasPosition) {
                            String position = getPosition(coreData.getUniqueId()).getName();
                            builder.append(separator).append('(').append(position).append(')');
                        }
                        if (headEmoji != null) {
                            builder.append(hasPosition ? " " : separator);
                            builder.append(headEmoji.getFormatted());
                        }
                    });
                    builder.append("\n\n");
                });
        TextChannel botCommandsChannel = DiscordConstants.getChannelBotCommands();
        builder.append("Apply in ").append(botCommandsChannel.getAsMention()).append(" via ").append(DiscordUtil.code("/apply"));
        builder.append("\nWe're always looking for new applicants!");

        String previousContent = message.getContentRaw();
        String newContent = builder.toString();

        if (previousContent.equals(newContent)) return;
        message.editMessage(newContent).queue();
    }

    private boolean hasMembers(LinkedList<BulkData> list, Rank rank) {
        return list
                .stream()
                .anyMatch(bulkData -> bulkData.getRank().isEqual(rank));
    }

    private Message getMessage() {
        Properties properties = propertiesService.getProperties();
        TextChannel channel = DiscordConstants.getChannelStaffList();
        return channel.getHistory().retrievePast(100)
                .complete()
                .stream()
                .filter(m -> m.getId().equals(properties.getStaffListMessageId()))
                .findFirst()
                .orElseGet(() -> {
                    Message message = channel.sendMessage("Waiting for executor heartbeat...").complete();
                    properties.setStaffListMessageId(message.getId());
                    propertiesService.update();
                    return message;
                });
    }

    public void updatePosition(UUID uuid, String position) {
        Bson query = Filters.eq(DatabaseUtil.PRIMARY_KEY, uuid.toString());

        if (position == null) {
            staffListExtras.remove(uuid);
            databaseService.delete(DatabaseCollection.STAFF_LIST, query).run();
            return;
        }
        StaffListData extra = staffListExtras.get(uuid);

        if (extra == null) {
            extra = new StaffListData(uuid);
            staffListExtras.put(uuid, extra);
        }
        extra.setPosition(StaffListPosition.get(position));
        databaseService.update(DatabaseCollection.STAFF_LIST, query, GsonUtil.toDocument(extra)).run();
    }

    public boolean hasPosition(UUID uuid) {
        return staffListExtras.containsKey(uuid);
    }

    public StaffListPosition getPosition(UUID uuid) {
        if (!hasPosition(uuid)) return null;
        return staffListExtras.get(uuid).getPosition();
    }

    public int getListPriority(UUID uuid) {
        return hasPosition(uuid) ? getPosition(uuid).ordinal() : Integer.MAX_VALUE;
    }
}

