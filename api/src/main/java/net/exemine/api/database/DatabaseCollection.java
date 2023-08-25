package net.exemine.api.database;

public enum DatabaseCollection {

    USERS_CORE,
    USERS_LOBBY,
    USERS_UHC,
    USERS_FFA,
    BULK_RANKS,
    BULK_PUNISHMENTS,
    BULK_PERMISSIONS,
    BULK_PLAYTIME,
    DISGUISE_NAMES,
    DISGUISE_SKINS,
    LOGS_VPN,
    LOGS_CHAT_DISCORD,
    LOGS_CHAT_MINECRAFT,
    TICKETS_APPLICATION,
    TICKETS_APPEAL,
    TICKETS_SUPPORT,
    MATCHES_UHC,
    TAGS,
    SKIN_TEXTURES,
    STAFF_LIST,
    PROPERTIES;

    @Override
    public String toString() {
        return name().toLowerCase().replace("_", "-");
    }
}
