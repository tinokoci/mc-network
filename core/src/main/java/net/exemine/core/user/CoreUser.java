package net.exemine.core.user;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.cosmetic.Cosmetic;
import net.exemine.api.cosmetic.CosmeticType;
import net.exemine.api.cosmetic.tag.Tag;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.model.Channel;
import net.exemine.api.model.ServerTime;
import net.exemine.api.properties.Properties;
import net.exemine.api.proxy.ProxyCheck;
import net.exemine.api.punishment.Punishment;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankType;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.cache.RedisCache;
import net.exemine.api.redis.cache.model.DiscordLinkModel;
import net.exemine.api.redis.cache.model.DisguiseModel;
import net.exemine.api.util.Executor;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.Core;
import net.exemine.core.cosmetic.bow.BowTrail;
import net.exemine.core.cosmetic.color.ColorType;
import net.exemine.core.cosmetic.rod.RodTrail;
import net.exemine.core.disguise.DisguiseService;
import net.exemine.core.disguise.action.DisguiseAction;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.InstanceUtil;
import net.exemine.core.util.spigot.Clickable;
import org.bukkit.Sound;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
@Setter
public class CoreUser extends ExeUser<CoreData> {

    private final Core plugin;
    private final UserService<CoreUser, CoreData> userService;

    private long sessionLoginTime;
    private UUID conversationPartner;
    private DiscordLinkModel discordLinkModel;

    private Punishment punishmentAllowConnect;

    public CoreUser(Class<CoreData> dataTypeClass, Core plugin) {
        super(dataTypeClass, plugin.getUserService(), plugin);
        this.plugin = plugin;
        this.userService = plugin.getUserService();
    }

    @Override
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        getData().updateGeneralData(event.getName(), event.getAddress().getHostAddress());

        // If network is under maintenance
        Properties properties = plugin.getPropertiesService().getProperties();
        Rank maintenanceRank = properties.getMaintenanceRank();

        if (properties.isMaintenance() && !isEqualOrAbove(maintenanceRank)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "The network is currently "
                    + (isEqualOrAbove(RankType.STAFF) ? "limited to " + maintenanceRank.getName() + " and above" : "under maintenance")
                    + ".\nMore information @ " + CC.UNDERLINE + Lang.DISCORD);
            return;
        }

        // If the server is full
        if (InstanceUtil.isFull(this, InstanceUtil.getCurrent())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "This instance is full!\nPurchase a rank @ " + Lang.STORE + " to bypass this restriction.");
            return;
        }

        // If user is not whitelisted
        if (InstanceUtil.isBlockedByWhitelist(this, InstanceUtil.getCurrent())) {
            Rank whitelistRank = InstanceUtil.getCurrent().getWhitelistRank();
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "This instance is currently "
                    + (isEqualOrAbove(RankType.STAFF) ? "limited to " + whitelistRank.getName() + " and above" : "under whitelist")
                    + ".");
            return;
        }
        punishmentAllowConnect = null;
        boolean allowConnect = !getData().isDiscordLinked() && InstanceUtil.isType(InstanceType.HUB);

        // If user has a punishment of BAN or greater
        getBulkData().getActivePunishments()
                .stream()
                .filter(punishment -> punishment.getType().isOrGreaterThan(PunishmentType.BAN))
                .findAny()
                .ifPresent(punishment -> {
                    if (handlePunishedAllowedToLink(allowConnect, punishment) && punishment.getType() != PunishmentType.BLACKLIST) return;
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, punishment.getLoginMessage());
                });

        // If user's alt has a punishment of IP BAN
        getAltAccounts(false)
                .stream()
                .filter(alt -> alt.getBulkData().getActivePunishment(PunishmentType.IP_BAN) != null)
                .findAny()
                .ifPresent(alt -> {
                    Punishment punishment = alt.getBulkData().getActivePunishment(PunishmentType.IP_BAN);
                    if (handlePunishedAllowedToLink(allowConnect, punishment)) return;
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, punishment.getLoginMessage());
                });

        // Check possibility of VPN usage
        ProxyCheck check = plugin.getProxyService().getOrCheckAddress(getData().getAddress());
        getBulkData().setTimeZone(TimeUtil.getTimeZone(check.getTimeZoneId()));

        if (check.isBlacklisted()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "Usage of a VPN is prohibited.\nIn case of a false positive, contact us @ " + Lang.DISCORD);
        }
    }

    @Override
    public void onJoin() {
        sessionLoginTime = System.currentTimeMillis();
        setupPowers();
        setTime();
        tryToDisguise();
        Executor.schedule(this::sendDiscordUpdate).runAsync();
    }

    @Override
    public void onQuit() {
        Executor.schedule(() -> {
            getBulkData().updatePlayTime(sessionLoginTime, InstanceUtil.getType());
            sessionLoginTime = 0L;
        }).runAsync();
    }

    private boolean handlePunishedAllowedToLink(boolean allowConnect, Punishment punishment) {
        if (allowConnect) {
            punishmentAllowConnect = punishment;
        }
        return allowConnect;
    }

    public boolean isPunishedAllowedConnectForLink() {
        return punishmentAllowConnect != null;
    }

    public void setupPowers() {
        if (isOnline()) setOp(isEqualOrAbove(Rank.DEVELOPER));
    }

    public void tryToDisguise() {
        DisguiseModel disguiseModel = plugin.getRedisService().getValueFromHash(RedisCache.DISGUISE, getUniqueId(), DisguiseModel.class);

        if (disguiseModel == null) {
            getBulkData().setDisguiseModel(null);
            return;
        }
        DisguiseService disguiseService = plugin.getDisguiseService();

        if (disguiseService.canAccess(this)) {
            disguiseService.disguise(this, disguiseModel.getName(), disguiseModel.getSkin(), DisguiseAction.REDISGUISE);
        } else {
            disguiseService.undisguise(this);
        }
    }

    public void setTime() {
        if (getData().getServerTime() == ServerTime.DEFAULT) {
            resetPlayerTime();
        } else {
            setPlayerTime(getData().getServerTime().getValue(), false);
        }
    }

    public void sendPrivateMessage(CoreUser receiver, String message) {
        Punishment punishment = getBulkData().getActivePunishment(PunishmentType.MUTE);

        if (punishment != null) {
            sendMessage(CC.RED + "You are currently suspended from the chat.");
            sendMessage(CC.RED + (punishment.isPermanent() ? "This punishment will not expire." : "Your punishment expires in " + punishment.getExpirationTime() + "."));
            return;
        }
        if (!receiver.getData().isPrivateMessages() && !isEqualOrAbove(RankType.STAFF)) {
            sendMessage(receiver.getColoredDisplayName() + CC.RED + " has private messages disabled.");
            return;
        }
        if (!receiver.getData().isIgnoring(getUniqueId()) || isEqualOrAbove(RankType.STAFF)) {
            getUserService().getOnlineUsers()
                    .stream()
                    .filter(user -> user.isEqualOrAbove(RankType.STAFF) && user.getStaffData().isSocialSpy() && user.isAbove(this) && user.isAbove(receiver))
                    .forEach(user -> user.sendMessage(CC.GOLD + "[SP] " + getColoredDisplayName() + CC.YELLOW + " -> " + receiver.getColoredDisplayName() + CC.YELLOW + ": " + message));

            if (receiver.getData().isMessagingSounds()) {
                receiver.playSound(Sound.SUCCESSFUL_HIT);
            }
            receiver.sendMessage(CC.GRAY + "(From " + getColoredDisplayName() + CC.GRAY + ") " + message);
            receiver.setConversationPartner(getUniqueId());
        }
        sendMessage(CC.GRAY + "(To " + receiver.getColoredDisplayName() + CC.GRAY + ") " + message);
        setConversationPartner(receiver.getUniqueId());
    }

    public void toggleChannel(Channel channel) {
        getData().setChannel(getData().getChannel() != channel ? channel : Channel.DEFAULT);
        saveData(true);

        boolean enable = getData().getChannel() == channel;

        sendMessage(CC.PURPLE + "[Chat] " + CC.GRAY + "You are " + StringUtil.formatBooleanCommand(enable) + CC.GRAY
                + " speaking in " + CC.GOLD + channel.name().toLowerCase() + CC.GRAY + " chat.");
    }

    public void toggleAltIgnore(CoreUser user, CoreUser target, boolean isAdded) {
        List<UUID> ignoredAlts = user.getData().getIgnoredAlts();
        if (isAdded) {
            ignoredAlts.remove(target.getUniqueId());
        } else {
            ignoredAlts.add(target.getUniqueId());
        }
        user.saveData(true);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public List<CoreUser> getAltAccounts(boolean includeIgnoredAlts) {
        return plugin.getDataService().fetchAll(CoreData.class)
                .stream()
                .filter(coreData -> !coreData.getUniqueId().equals(getUniqueId()))
                .filter(coreData -> (includeIgnoredAlts || !getData().isAltIgnored(coreData.getUniqueId()))
                        && coreData.getAddresses()
                        .stream()
                        .anyMatch(address -> getData().getAddresses().contains(address)))
                .map(coreData -> userService.fetch(coreData.getUniqueId()).get())
                .sorted(Comparator.comparing(alt -> alt.getBulkData().getActivePunishment(PunishmentType.IP_BAN, PunishmentType.BAN) != null, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    public boolean getDiscordLinkRequest(RedisService redisService) {
        discordLinkModel = redisService.getValueFromHash(RedisCache.DISCORD_LINK, getUniqueId(), DiscordLinkModel.class);

        if (discordLinkModel == null) {
            return false;
        }
        new Clickable()
                .add(CC.PURPLE + "[Link] " + CC.GOLD + discordLinkModel.getUserName() + CC.GRAY + " from discord has requested to link with your account. ")
                .add(CC.GREEN + "[Accept]", CC.GREEN + "Click to link!", "/link accept")
                .send(this);
        return true;
    }

    public void sendDiscordUpdate() {
        plugin.getLinkService().sendDiscordUpdate(getCoreData(), getBulkData());
    }

    @Nullable
    public Tag getTag() {
        return getCosmetic(() -> plugin.getTagService().getTag(getCosmeticData().getTag()));
    }

    @Nullable
    public ColorType getColorType() {
        return getCosmetic(() -> ColorType.get(getCosmeticData().getColorType()));
    }

    @Nullable
    public BowTrail getBowTrail() {
        return getCosmetic(() -> BowTrail.get(getCosmeticData().getBowTrail()));
    }

    @Nullable
    public RodTrail getRodTrail() {
        return getCosmetic(() -> RodTrail.get(getCosmeticData().getRodTrail()));
    }

    private <T extends Cosmetic> T getCosmetic(Supplier<T> supplier) {
        T cosmetic = supplier.get();
        if (cosmetic == null || !hasUnlockedCosmetic(cosmetic)) return null;
        return cosmetic;
    }

    public boolean hasUnlockedCosmetic(Cosmetic cosmetic) {
        return isEqualOrAbove(Rank.DEVELOPER)
                || hasPermission(cosmetic.getPermission())
                || (cosmetic.getType() == CosmeticType.COLOR && hasRankBetween(Rank.ALPHA, Rank.RETIRED));
    }

    public long getSessionLoginTime() {
        return isOnline() ? sessionLoginTime : 0L;
    }

    public long getSessionLoginTime(boolean predicate) {
        return predicate ? getSessionLoginTime() : 0L;
    }

    public CoreData.StaffData getStaffData() {
        return getData().getStaffData();
    }

    public CoreData.CosmeticData getCosmeticData() {
        return getData().getCosmeticData();
    }

    public boolean canModifyRank(Rank rank) {
        return isEqualOrAbove(Rank.OWNER) || (isEqualOrAbove(Rank.MANAGER) && isAbove(rank));
    }
}