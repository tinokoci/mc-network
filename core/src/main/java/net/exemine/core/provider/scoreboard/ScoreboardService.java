package net.exemine.core.provider.scoreboard;

import lombok.AccessLevel;
import lombok.Getter;
import net.exemine.api.util.Executor;
import net.exemine.core.provider.ScoreboardProvider;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

@Getter(AccessLevel.PACKAGE)
public class ScoreboardService<T extends ExeUser<?>> {

    @Getter
    private static ScoreboardService<?> instance;

    public static ScoreboardService<?> get() {
        return instance;
    }

    private final UserService<T, ?> userService;
    private final ScoreboardProvider<T> provider;

    private ScheduledFuture<?> scheduledFuture;

    private final Map<UUID, PlayerScoreboard<T>> scoreboards = new HashMap<>();

    @Getter
    private long intervalInMillis = 90L;

    public ScoreboardService(JavaPlugin plugin, UserService<T, ?> userService, ScoreboardProvider<T> provider) {
        this.userService = userService;
        this.provider = provider;

        instance = this;
        ScoreboardListener<T> listener = new ScoreboardListener<T>(this, userService);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        schedule();
    }

    private void schedule() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        scheduledFuture = Executor
                .schedule(() -> {
                    provider.beforeUpdate();

                    userService.getOnlineUsers().forEach(user -> {
                        PlayerScoreboard<T> board = scoreboards.get(user.getUniqueId());
                        if (board == null) return;

                        board.clear();
                        provider.loadObjectives(user, board.getScoreboard());
                        provider.update(user, board);
                    });
                })
                .runAsyncTimer(0, intervalInMillis);
    }

    public void reschedule(long intervalInMillis) {
        this.intervalInMillis = intervalInMillis;
        schedule();
    }

    void setup(T user) {
        scoreboards.put(user.getUniqueId(), new PlayerScoreboard<>(user, this));
    }

    void destroy(T user) {
        scoreboards.remove(user.getUniqueId());
    }
}