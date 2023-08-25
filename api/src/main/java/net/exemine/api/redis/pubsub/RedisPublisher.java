package net.exemine.api.redis.pubsub;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.match.Match;
import net.exemine.api.proxy.ProxyCheckState;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.rank.Rank;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.model.AlertStaffMessageModel;
import net.exemine.api.redis.pubsub.model.AlertStaffServerSwitchModel;
import net.exemine.api.redis.pubsub.model.AlertUHCAnnounceModel;
import net.exemine.api.redis.pubsub.model.DiscordUserUpdateModel;
import net.exemine.api.redis.pubsub.model.InstanceCommandModel;
import net.exemine.api.redis.pubsub.model.InstanceHeartbeatModel;
import net.exemine.api.redis.pubsub.model.ProxyCheckStateUpdateModel;
import net.exemine.api.redis.pubsub.model.PunishmentExecutionModel;
import net.exemine.api.redis.pubsub.model.RankUpdateModel;
import net.exemine.api.redis.pubsub.model.TagCreateModel;
import net.exemine.api.redis.pubsub.model.generic.StringModel;
import net.exemine.api.redis.pubsub.model.generic.UUIDModel;
import net.exemine.api.texture.TextureEntry;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisService redisService;

    public void sendAlertStaffMessage(Rank rank, String message) {
        redisService.publish(RedisMessage.ALERT_STAFF_MESSAGE, new AlertStaffMessageModel(rank, message));
    }

    public void sendAlertStaffServerSwitch(UUID uuid, String message) {
        redisService.publish(RedisMessage.ALERT_STAFF_SERVER_SWITCH, new AlertStaffServerSwitchModel(uuid, message));
    }

    public void sendAlertUHCAnnounce(String description, String clickable, String hover, String action) {
        redisService.publish(RedisMessage.ALERT_UHC_ANNOUNCE, new AlertUHCAnnounceModel(description, clickable, hover, action));
    }

    public void sendCoreDataUpdate(UUID uuid) {
        redisService.publish(RedisMessage.CORE_DATA_UPDATE, new UUIDModel(uuid));
    }

    public void sendDiscordLinkRequest(UUID uuid) {
        redisService.publish(RedisMessage.DISCORD_LINK_REQUEST, new UUIDModel(uuid));
    }

    public void sendDiscordUnlinkRequest(String userId) {
        redisService.publish(RedisMessage.DISCORD_UNLINK_REQUEST, new StringModel(userId));
    }

    public void sendDisguiseEntriesRefresh() {
        redisService.publish(RedisMessage.DISGUISE_ENTRIES_REFRESH, new Object());
    }

    public void updateDiscordUser(String userId, String name, List<Rank> ranks, boolean locked) {
        redisService.publish(RedisMessage.DISCORD_USER_UPDATE, new DiscordUserUpdateModel(userId, name, ranks, locked));
    }

    public void sendInstanceCommand(String name, String command) {
        redisService.publish(RedisMessage.INSTANCE_COMMAND, new InstanceCommandModel(name, command));
    }

    public void sendInstanceHeartbeat(String name, InstanceType type, List<String> onlinePlayers, int maxPlayers, double tps1, double tps2, double tps3, Rank whitelistRank, JsonObject extra) {
        redisService.publish(RedisMessage.INSTANCE_HEARTBEAT, new InstanceHeartbeatModel(name, type, onlinePlayers, onlinePlayers.size(), maxPlayers, tps1, tps2, tps3, whitelistRank, extra));
    }

    public void sendInstanceShutdown(String name) {
        redisService.publish(RedisMessage.INSTANCE_SHUTDOWN, new StringModel(name));
    }

    public void sendPermissionsUpdate(UUID uuid) {
        redisService.publish(RedisMessage.PERMISSIONS_UPDATE, new UUIDModel(uuid));
    }

    public void sendPlayerReport(String message) {
        redisService.publish(RedisMessage.PLAYER_REPORT, new StringModel(message));
    }

    public void sendPropertiesUpdate() {
        redisService.publish(RedisMessage.PROPERTIES_UPDATE, new Object());
    }

    public void sendProxyCheckStateUpdate(String address, ProxyCheckState state) {
        redisService.publish(RedisMessage.PROXY_CHECK_STATE_UPDATE, new ProxyCheckStateUpdateModel(address, state));
    }

    public void sendPunishmentExecution(UUID uuid, PunishmentType type, int index) {
        redisService.publish(RedisMessage.PUNISHMENT_EXECUTION, new PunishmentExecutionModel(uuid, type, index));
    }

    public void sendPunishmentsUpdate(UUID uuid) {
        redisService.publish(RedisMessage.PUNISHMENTS_UPDATE, new UUIDModel(uuid));
    }

    public void sendRankUpdate(UUID uuid, boolean mainRankChanged) {
        redisService.publish(RedisMessage.RANK_UPDATE, new RankUpdateModel(uuid, mainRankChanged));
    }

    public void sendTagCreate(String name, String format) {
        redisService.publish(RedisMessage.TAG_CREATE, new TagCreateModel(name, format));
    }

    public void sendTagDelete(String name) {
        redisService.publish(RedisMessage.TAG_DELETE, new StringModel(name));
    }

    public void sendTextureAdd(TextureEntry entry) {
        redisService.publish(RedisMessage.TEXTURE_ADD, entry);
    }

    public void sendUHCSetup(Match match) {
        redisService.publish(RedisMessage.UHC_SETUP, match);
    }
}
