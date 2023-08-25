package net.exemine.core.provider.nametag;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.util.Executor;
import net.exemine.core.provider.NametagProvider;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.team.TeamPriority;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Getter
@Setter
public class NametagService<T extends ExeUser<?>> implements Listener {

    private static NametagService<?> instance;

    public static NametagService<?> get() {
        return instance;
    }

    private final UserService<T, ?> userService;
    private final NametagProvider<T> provider;

    private ScheduledFuture<?> scheduledFuture;

    private List<NametagInfo> registeredTeams = Collections.synchronizedList(new ArrayList<>());
    private Map<T, List<String>> sentTeamNames = new ConcurrentHashMap<>();
    private Map<T, Map<T, NametagInfo>> nametagTracker = new ConcurrentHashMap<>();

    private int teamCreateIndex = 1;

    @Getter
    private long intervalInMillis = 500L;

    public NametagService(JavaPlugin plugin, UserService<T, ?> userService, NametagProvider<T> adapter) {
        this.userService = userService;
        this.provider = adapter;

        instance = this;
        NametagListener<T> listener = new NametagListener<T>(this, userService);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        schedule();
    }

    private void schedule() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        scheduledFuture = Executor
                .schedule(() -> userService.getOnlineUsers().forEach(this::setupNametagsFor))
                .runAsyncTimer(0, intervalInMillis);
    }

    public void reschedule(long intervalInMillis) {
        this.intervalInMillis = intervalInMillis;
        schedule();
    }

    public void setupNametagsOf(UUID toSetup) {
        setupNametagsOf(userService.get(toSetup));
    }

    public void setupNametagsOf(T toSetup) {
        userService.getOnlineUsers()
                .stream()
                .filter(user -> user != toSetup)
                .forEach(user -> {
                    Map<T, NametagInfo> userNametagTracker = nametagTracker.getOrDefault(user, new HashMap<>());
                    setupNametags(user, toSetup, userNametagTracker);
                });
    }

    public void setupNametagsFor(T user) {
        Map<T, NametagInfo> userNametagTracker = nametagTracker.getOrDefault(user, new HashMap<>());
        userService.getOnlineUsers().forEach(toSetup -> setupNametags(user, toSetup, userNametagTracker));
    }

    private void setupNametags(T user, T toSetup, Map<T, NametagInfo> userNametagTracker) {
        NametagInfo newNametagInfo = provider.getNametag(toSetup, user, this);
        NametagInfo previousNametagInfo = userNametagTracker.get(toSetup);

        // don't update nametag if there's no updates
        if (previousNametagInfo != null
                && previousNametagInfo.equals(newNametagInfo)
                && previousNametagInfo.hasMember(user.getDisplayName())) return;

        String member = toSetup.getDisplayName();
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(newNametagInfo.getName(), List.of(member), 3);

        user.sendPacket(packet);
        newNametagInfo.addMember(member);
        userNametagTracker.put(toSetup, newNametagInfo); // insert new NametagInfo into tracker
    }

    public NametagInfo getOrCreate(String teamName, String prefix) {
        return getOrCreate(teamName, prefix, "");
    }

    public NametagInfo getOrCreate(String teamName, String prefix, String suffix) {
        return registeredTeams
                .stream()
                .filter(teamInfo -> teamInfo.getPrefix().equals(prefix) && teamInfo.getSuffix().equals(suffix))
                .findFirst()
                .orElseGet(() -> {
                    NametagInfo newTeam = new NametagInfo(TeamPriority.NAMETAG + "-" + teamName + '-' + teamCreateIndex++, prefix, suffix);
                    registeredTeams.add(newTeam);
                    userService.getOnlineUsers().forEach(player -> sendTeamCreatePacket(player, newTeam));
                    return newTeam;
                });
    }

    private void sendTeamCreatePacket(T user, NametagInfo nametagInfo) {
        List<String> teamNames = sentTeamNames.getOrDefault(user, new ArrayList<>());
        if (teamNames.contains(nametagInfo.getName())) return;

        teamNames.add(nametagInfo.getName());
        user.sendPacket(nametagInfo.getTeamAddPacket());

        if (!sentTeamNames.containsKey(user)) {
            sentTeamNames.put(user, teamNames);
        }
    }

    void setup(T user) {
        registeredTeams.forEach(info -> sendTeamCreatePacket(user, info));
        nametagTracker.put(user, new HashMap<>());

        // load nametags immediately
        Executor.schedule(() -> {
            setupNametagsFor(user);
            setupNametagsOf(user);
        }).runAsync();
    }

    void destroy(T user) {
        nametagTracker.remove(user);
        sentTeamNames.remove(user);
    }
}