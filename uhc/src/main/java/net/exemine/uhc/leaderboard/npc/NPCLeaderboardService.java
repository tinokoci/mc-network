package net.exemine.uhc.leaderboard.npc;

import lombok.Getter;
import net.exemine.api.data.impl.UHCData;
import net.exemine.api.data.stat.TimedStatSpan;
import net.exemine.api.leaderboard.LeaderboardData;
import net.exemine.api.leaderboard.LeaderboardService;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.api.util.string.CC;
import net.exemine.core.nms.hologram.Hologram;
import net.exemine.core.nms.npc.NPC;
import net.exemine.core.util.LocationUtil;
import net.exemine.uhc.UHC;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class NPCLeaderboardService {

    private final ConfigFile config;
    private final LeaderboardService<UHCData> leaderboardService;

    private final Map<Integer, NPCLeaderboard> npcLeaderboards = new HashMap<>();
    private final String configPath = "leaderboard.npc.";
    private final String key = "wins";

    public NPCLeaderboardService(UHC plugin, ConfigFile config, LeaderboardService<UHCData> leaderboardService) {
        this.config = config;
        this.leaderboardService = leaderboardService;

        Location firstLocation = LocationUtil.deserializeLocation(config.getString(configPath + "first"));
        Location secondLocation = LocationUtil.deserializeLocation(config.getString(configPath + "second"));
        Location thirdLocation = LocationUtil.deserializeLocation(config.getString(configPath + "third"));

        new NPCLeaderboardTask(plugin, leaderboardService, this);
        Bukkit.getPluginManager().registerEvents(new NPCLeaderboardListener(this), plugin);

        List<LeaderboardData> leaderboards = leaderboardService.getByKey(key, TimedStatSpan.GLOBAL);

        if (leaderboards.size() >= 3) {
            String title = "Top Wins";
            if (firstLocation != null) create(title, leaderboards.get(0), firstLocation);
            if (secondLocation != null) create(title, leaderboards.get(1), secondLocation);
            if (thirdLocation != null) create(title, leaderboards.get(2), thirdLocation);
        }
    }

    public void create(String title, LeaderboardData data, Location location) {
        Hologram hologram = new Hologram(CC.BOLD_PINK + title);
        hologram.addLineBelow(data.getDisplayName() + CC.GRAY + " - " + CC.WHITE + data.getStat().getInTimeSpan(TimedStatSpan.GLOBAL));

        NPC npc = new NPC(CC.GOLD + '#' + data.getPlacing(), data.getName(), location, true).attachHologram(hologram);
        npcLeaderboards.put(npc.getEntityId(), new NPCLeaderboard(npc, data.getName(), data.getPlacing()));
    }

    // todo: use this in location command when ConfigFile is updated
    public void updateLocation(String placing, Location location) {
        config.set(configPath + placing, LocationUtil.serializeLocation(location));
        config.save();
    }

    public NPCLeaderboard getLeaderboard(int id) {
        return npcLeaderboards.get(id);
    }
}

