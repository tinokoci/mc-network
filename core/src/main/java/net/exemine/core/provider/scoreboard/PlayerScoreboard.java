package net.exemine.core.provider.scoreboard;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.provider.ScoreboardProvider;
import net.exemine.core.user.base.ExeUser;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerScoreboard<T extends ExeUser<?>> {

    private final T user;

    @Getter
    private final Scoreboard scoreboard;

    private final Deque<ScoreboardInput> entries = new ArrayDeque<>();
    private final Map<String, Integer> displayedEntries = new HashMap<>();
    private final Map<String, String> scorePrefixes = new HashMap<>();
    private final Map<String, String> scoreSuffixes = new HashMap<>();

    private final Set<String> recentlyUpdatedScores = new HashSet<>();
    private final Set<String> sentTeamCreates = new HashSet<>();

    PlayerScoreboard(T user, ScoreboardService<T> scoreboardService) {
        this.user = user;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = this.scoreboard.registerNewObjective(Lang.SERVER_NAME.toLowerCase(), "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        ScoreboardProvider<T> adapter = scoreboardService.getProvider();
        objective.setDisplayName(adapter.getTitle());
        user.setScoreboard(this.scoreboard);
    }

    public void update() {
        recentlyUpdatedScores.clear();

        for (int i = entries.size(); i > 0; i--) {
            ScoreboardInput input = entries.pollFirst();

            if (input == null) return;

            String score = input.getName();
            recentlyUpdatedScores.add(score);

            if (!sentTeamCreates.contains(score)) {
                createAndAddMember(score);
            }
            if (displayedEntries.getOrDefault(score, -1) != i) {
                setScore(score, i);
            }
            String prefix = input.getPrefix();
            String suffix = input.getSuffix();

            if (!scorePrefixes.containsKey(score)
                    || !scorePrefixes.get(score).equals(prefix)
                    || !scoreSuffixes.get(score).equals(suffix)) {
                updateScore(score, prefix, suffix);
            }
        }
        for (String score : ImmutableSet.copyOf(displayedEntries.keySet())) {
            if (recentlyUpdatedScores.contains(score)) continue;
            removeScore(score);
        }
    }

    public void add(String prefix, String name, String suffix) {
        if (entries.size() > 15) return;
        entries.addLast(new ScoreboardInput(prefix, name, suffix));
    }

    public void add() {
        add("");
    }

    public void add(String line) {
        // Gets a random chat color and returns a string of it
        String name = ChatColor.values()[entries.size()].toString() + CC.RESET;

        // Every attribute (prefix, name, suffix) can contain max 16 characters so we must do a work-around
        if (line.length() > 16) {
            // If there's a color code at the end, make it push that code into the suffix too
            int split = line.charAt(15) == ChatColor.COLOR_CHAR ? 15 : 16;

            String prefix = line.substring(0, split);
            String suffix = ChatColor.getLastColors(prefix) + line.substring(split);

            add(prefix, name, suffix.length() > 16 ? suffix.substring(0, 16) : suffix);
        } else {
            add(line, name, "");
        }
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public void clear() {
        entries.clear();
    }

    private PacketPlayOutScoreboardTeam getTeamPacket(String name, String prefix, String suffix, String member, int action) {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();

        packet.a = name; // team name
        packet.f = action; // action

        if (action != 3) {
            packet.b = name; // name
            packet.c = prefix; // prefix
            packet.d = suffix; // suffix
            packet.g = 3; // some other action
        }

        if (action == 0 || action == 3) {
            packet.e.add(member); // text
        }
        return packet;
    }

    private void updateScore(String score, String prefix, String suffix) {
        scorePrefixes.put(score, prefix);
        scoreSuffixes.put(score, suffix);

        user.sendPacket(getTeamPacket(score, prefix, suffix, null, 2));
    }

    private void createAndAddMember(String score) {
        user.sendPacket(getTeamPacket(score, "_", "_", "", 0));
        user.sendPacket(getTeamPacket(score, null, null, score, 3));
        sentTeamCreates.add(score);
    }

    private void setScore(String score, int value) {
        PacketPlayOutScoreboardScore scoreboardScorePacket = new PacketPlayOutScoreboardScore();
        scoreboardScorePacket.a = score;
        scoreboardScorePacket.b = Lang.SERVER_NAME.toLowerCase();
        scoreboardScorePacket.c = value;
        scoreboardScorePacket.d = PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE;

        displayedEntries.put(score, value);
        user.sendPacket(scoreboardScorePacket);
    }

    private void removeScore(String score) {
        displayedEntries.remove(score);
        scorePrefixes.remove(score);
        scoreSuffixes.remove(score);

        user.sendPacket(new PacketPlayOutScoreboardScore(score));
    }

    @Getter
    @RequiredArgsConstructor
    public static class ScoreboardInput {

        private final String prefix, name, suffix;
    }
}