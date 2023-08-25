package net.exemine.api.redis.pubsub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RedisMessage {

    ALERT_STAFF_MESSAGE,
    ALERT_STAFF_SERVER_SWITCH,
    ALERT_UHC_ANNOUNCE,
    CORE_DATA_UPDATE,
    DISCORD_LINK_REQUEST,
    DISCORD_UNLINK_REQUEST,
    DISCORD_USER_UPDATE,
    DISGUISE_ENTRIES_REFRESH,
    INSTANCE_COMMAND,
    INSTANCE_HEARTBEAT,
    INSTANCE_SHUTDOWN,
    PERMISSIONS_UPDATE,
    PLAYER_REPORT,
    PROPERTIES_UPDATE,
    PROXY_CHECK_STATE_UPDATE,
    PUNISHMENT_EXECUTION,
    PUNISHMENTS_UPDATE,
    RANK_UPDATE,
    TAG_CREATE,
    TAG_DELETE,
    TEXTURE_ADD,
    UHC_SETUP
}
