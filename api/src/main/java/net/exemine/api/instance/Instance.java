package net.exemine.api.instance;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.controller.ApiController;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class Instance {

    private final String name;
    private final InstanceType type;

    private final long bootTime = System.currentTimeMillis();
    private long lastUpdate;

    private Rank whitelistRank = Rank.DEFAULT;
    private List<String> playerNames;

    private int onlinePlayers;
    private int maxPlayers;
    private int vanishedPlayers;
    private double tps1, tps2, tps3;

    private JsonObject extra = new JsonObject();

    public void setExtra(Object object) {
        this.extra = GsonUtil.toJsonObject(object);
    }

    public boolean isNotReceivingHeartbeats() {
        return System.currentTimeMillis() - lastUpdate > 10_000L;
    }

    public boolean isOffline() {
        return name == null || isNotReceivingHeartbeats();
    }

    public boolean isOnline() {
        return !isOffline();
    }

    public boolean isWhitelisted() {
        return whitelistRank != Rank.DEFAULT;
    }

    public boolean isJoinable() {
        return isOnline() && !isWhitelisted() && !isFull();
    }

    public boolean isFull() {
        return getOnlinePlayers() >= getMaxPlayers();
    }

    public String getFormattedUptime() {
        return TimeUtil.getNormalDuration(System.currentTimeMillis() - bootTime);
    }

    public String getStatus(boolean detailedWhitelist) {
        ApiController.requireMinecraftPlatform();
        if (isOffline()) return CC.RED + "Offline";
        if (isJoinable()) return CC.GREEN + "Online";
        return detailedWhitelist ? whitelistRank.getDisplayName() + " Whitelist" : CC.RED + "Whitelisted";
    }
}
