package net.exemine.api.data.impl;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.ExeData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.model.Channel;
import net.exemine.api.model.ServerTime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CoreData extends ExeData {

    private String address;
    private LocalDateTime firstJoined;

    private final List<String> oldAddresses = new ArrayList<>();
    private final List<UUID> ignoredAlts = new ArrayList<>();
    private final List<UUID> ignoreList = new ArrayList<>();

    private Channel channel = Channel.DEFAULT;
    private ServerTime serverTime = ServerTime.DEFAULT;

    private boolean privateMessages = true;
    private boolean messagingSounds = true;
    private boolean particles = true;
    private boolean gameBroadcasts = true;
    private boolean serverTips = true;
    private boolean bossBar = true;
    private boolean scoreboard = true;
    private boolean tablist = true;

    private long totalPlayTime;
    private int level = 1;

    private String discordUserId;
    private boolean discordLocked;

    private final CosmeticData cosmeticData = new CosmeticData();
    private final StaffData staffData = new StaffData();
    private final LunarData lunarData = new LunarData();

    public void updateGeneralData(String name, String address) {
        setName(name);
        setAddress(address);

        if (firstJoined == null) {
            firstJoined = LocalDateTime.now();
        }
    }

    public void setAddress(String newAddress) {
        boolean addressHasChanged = address != null && !address.equals(newAddress);

        if (addressHasChanged) {
            oldAddresses.add(address); // Add previous main address to old addresses
            oldAddresses.remove(newAddress); // New address in old addresses? -> Remove, will be stored in main address var
        }
        address = newAddress;
    }

    public List<String> getAddresses() {
        List<String> addresses = new ArrayList<>();

        addresses.add(address);
        addresses.addAll(oldAddresses);

        return addresses;
    }

    public boolean isAltIgnored(UUID uuid) {
        return ignoredAlts.contains(uuid);
    }

    public boolean isIgnoring(UUID uuid) {
        return ignoreList.contains(uuid);
    }

    public boolean isDiscordLinked() {
        return discordUserId != null;
    }

    @Override
    public DatabaseCollection getMongoCollection() {
        return DatabaseCollection.USERS_CORE;
    }

    @Getter
    @Setter
    public static class CosmeticData {

        private String tag;
        private String colorType;
        private String rodTrail;
        private String bowTrail;
    }

    @Getter
    @Setter
    public static class StaffData {

        private boolean chatMessages = true;
        private boolean serverSwitch = true;
        private boolean reports = true;
        private boolean socialSpy;
        private boolean instanceAlerts;
    }

    @Getter
    @Setter
    public static class LunarData {

        private boolean titles = true;
        private boolean border = false;
        private boolean teamView = true;
        private boolean waypoints = false;
        private boolean nametags = false;
    }
}