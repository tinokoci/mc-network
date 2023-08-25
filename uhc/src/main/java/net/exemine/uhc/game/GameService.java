package net.exemine.uhc.game;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.match.Match;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.util.string.CC;
import net.exemine.core.lunar.impl.LunarTitle;
import net.exemine.core.util.MessageUtil;
import net.exemine.core.util.spigot.Clickable;
import net.exemine.core.util.spigot.PlayerImpl;
import net.exemine.uhc.UHC;
import net.exemine.uhc.autostart.AutoStartTask;
import net.exemine.uhc.border.task.BorderShrinkTask;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.game.event.GameEndEvent;
import net.exemine.uhc.game.event.GameStartEvent;
import net.exemine.uhc.game.sync.CalendarSetupTask;
import net.exemine.uhc.game.sync.UHCMatchSyncTask;
import net.exemine.uhc.game.task.EndTask;
import net.exemine.uhc.game.task.GameTask;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.team.TeamViewUpdateTask;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
@Setter
public class GameService {

    private final UHC plugin;
    private final UHCUserService userService;

    private final int matchNumber;
    private UHCMatch match;
    private boolean useMatchSynchronization = true;

    private GameState state = GameState.WORLD_GENERATION;
    private GameTask task;
    private long startTime;
    private long endTime;
    private int initialPlayers;

    public final List<UUID> whitelistedUUIDs = new ArrayList<>();
    private boolean spectatorChatMuted;

    private AutoStartTask autoStartTask;
    private boolean autoAssignRunning;

    public GameService(UHC plugin) {
        this.plugin = plugin;
        this.userService = plugin.getUserService();
        this.matchNumber = (int) (plugin.getCore().getMatchService().getAllMatches(UHCMatch.class)
                        .stream()
                        .filter(Match::isCompleted)
                        .count() + 1);
        new CalendarSetupTask(plugin);
        new UHCMatchSyncTask(plugin);
    }

    public void startGame() {
        startTime = System.currentTimeMillis();
        task = new GameTask(plugin);
        state = GameState.PLAYING;

        List<UHCUser> scatterUsers = userService.getAllScatterUsers();
        initialPlayers = scatterUsers.size();

        scatterUsers.forEach(scatterUser -> {
            if (scatterUser.isOnline()) {
                scatterUser.setState(UHCUserState.IN_GAME);
            }
        });

        MessageUtil.send("");
        MessageUtil.send(CC.BOLD_GREEN + "The game has started, good luck everyone!");
        MessageUtil.send(CC.ITALIC_GRAY + "(Don't forget to thank your host with /thanks)");
        MessageUtil.send("");
        MessageUtil.play(Sound.ENDERDRAGON_GROWL);

        Bukkit.getPluginManager().callEvent(new GameStartEvent());
        new TeamViewUpdateTask(plugin);
        plugin.getWorldService().setWorldsUsed(true);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void checkIfGameShouldEnd() {
        if (state != GameState.PLAYING) return;

        List<Team> aliveTeams = plugin.getTeamService().getAliveTeams();
        int aliveTeamsCount = aliveTeams.size();

        // Set state to end
        if (aliveTeamsCount == 1) {
            endTime = System.currentTimeMillis();
            state = GameState.ENDING;
            userService.getOnlineUsers().forEach(PlayerImpl::resetPlayerTime);
            plugin.getWorldService().getUhcWorld().setTime(18000L);

            // Send Win Message
            Team winningTeam = aliveTeams.get(0);

            MessageUtil.send(CC.STRIKETHROUGH_GRAY + "--------------------------------------------------");
            MessageUtil.send(MessageUtil.center(CC.BOLD_PINK + "   UHC"));
            MessageUtil.send("");

            if (isTeamGame()) {
                MessageUtil.send(MessageUtil.center("Winner" + (winningTeam.getSize() > 1 ? "s" : "") + ": "
                        + winningTeam.getMembers()
                        .stream()
                        .map(UHCUser::getColoredDisplayName)
                        .collect(Collectors.joining(CC.GRAY + ", ")))
                );
                new Clickable(MessageUtil.center(CC.GRAY + "[Top Kills Inventory]"),
                        CC.GREEN + "Click to view inventory!",
                        "/invsee " + winningTeam.getMembers()
                                .stream()
                                .max(Comparator.comparing(user -> user.getGameInfo().getKills().getValue()))
                                .get()
                                .getDisplayName())
                        .broadcast();
            } else {
                UHCUser winner = winningTeam.getMembers()
                        .stream()
                        .filter(UHCUser::isPlaying)
                        .findFirst()
                        .get();
                MessageUtil.send(MessageUtil.center("Winner: " + CC.WHITE + winner.getColoredDisplayName()));

                new Clickable(MessageUtil.center(CC.GRAY + "[Winner's Inventory]"),
                        CC.GREEN + "Click to view inventory!",
                        "/invsee " + winner.getRealName()
                ).broadcast();
            }
            MessageUtil.send("");
            MessageUtil.send(MessageUtil.center(CC.PINK + "   Top Kills"));

            AtomicInteger ranking = new AtomicInteger();
            userService.values()
                    .stream()
                    .sorted(Comparator.comparing(user -> user.getGameInfo().getKills().getValue(), Comparator.reverseOrder()))
                    .limit(3)
                    .forEach(user -> MessageUtil.send(MessageUtil.center(CC.GRAY + '#' + ranking.incrementAndGet() + ' ' + user.getColoredDisplayName() + CC.GRAY + " - " + CC.RESET + user.getGameInfo().getKills().getValue() + " Kills")));
            MessageUtil.send(CC.STRIKETHROUGH_GRAY + "--------------------------------------------------");

            for (UHCUser winner : winningTeam.getMembers()) {
                new LunarTitle(CC.BOLD_GOLD + "Congratulations!")
                        .setSubtitleMessage(CC.GREEN + "You win!")
                        .setDisplayTimeMs(10_000L)
                        .send(winner);
            }

            // Call game end event
            Bukkit.getPluginManager().callEvent(new GameEndEvent(winningTeam));
            new EndTask(plugin);

            // Cancel tasks because they are not important anymore
            BorderShrinkTask borderShrinkTask = plugin.getBorderService().getBorderShrinkTask();
            if (borderShrinkTask != null) {
                borderShrinkTask.cancel();
            }
            GameTask gameTask = plugin.getGameService().getTask();
            if (gameTask != null) {
                gameTask.cancel();
            }
        }
    }

    public boolean isMatchSynchronized() {
        return match != null;
    }

    public boolean isState(GameState... states) {
        return Arrays.stream(states).anyMatch(state -> this.state == state);
    }

    public boolean isStateOrHigher(GameState state) {
        return this.state.ordinal() >= state.ordinal();
    }

    public boolean isStateOrLower(GameState state) {
        return this.state.ordinal() <= state.ordinal();
    }

    public boolean isNotState(GameState... states) {
        return Arrays.stream(states).noneMatch(state -> this.state == state);
    }

    public boolean isTeamGame() {
        return NumberOption.PLAYERS_PER_TEAM.getValue() > 1;
    }

    public boolean isSoloGame() {
        return !isTeamGame();
    }

    public String getFormattedMode() {
        if (isSoloGame()) return "FFA";
        return "To" + NumberOption.PLAYERS_PER_TEAM.getValue();
    }

    public boolean isPvP() {
        return plugin.getWorldService().getUhcWorld().getPVP();
    }

    public void setPvP(boolean pvp) {
        WorldService worldService = plugin.getWorldService();
        worldService.getUhcWorld().setPVP(pvp);
        worldService.getNetherWorld().setPVP(pvp);
    }

    public UHCUser getHost() {
        List<UHCUser> hosts = userService.getHostUsers();
        return hosts.isEmpty() ? null : hosts.get(0);
    }

    public UHCUser getSupervisor() {
        List<UHCUser> supervisors = userService.getSupervisorUsers();
        return supervisors.isEmpty() ? null : supervisors.get(0);
    }

    public boolean isSupervised() {
        return getSupervisor() != null;
    }

    public String getFormattedHost() {
        UHCUser host = getHost();
        if (host == null) return "None";
        if (host.isOnline()) return host.getColoredRealName();
        return host.getRank().getColor() + CC.STRIKETHROUGH + host.getRealName();
    }

    public String getFormattedSupervisor() {
        UHCUser supervisor = getSupervisor();
        if (supervisor == null) return "None";
        if (supervisor.isOnline()) return supervisor.getColoredRealName();
        return supervisor.getRank().getColor() + CC.STRIKETHROUGH + supervisor.getRealName();
    }

    public boolean isWhitelisted(UHCUser user) {
        return whitelistedUUIDs.contains(user.getUniqueId());
    }

    public void addToWhitelist(UHCUser user) {
        whitelistedUUIDs.add(user.getUniqueId());
    }

    public void removeFromWhitelist(UHCUser user) {
        whitelistedUUIDs.remove(user.getUniqueId());
    }

    public void clearWhitelist() {
        whitelistedUUIDs.clear();
    }

    public List<UHCUser> getWhitelistedUsers() {
        return whitelistedUUIDs
                .stream()
                .map(userService::retrieve)
                .collect(Collectors.toList());
    }

    public boolean isScheduledToAutoStart() {
        return autoStartTask != null;
    }

    public void cancelAutoStart() {
        if (autoStartTask == null) return;
        autoStartTask.cancel();
        autoStartTask = null;
    }
}
