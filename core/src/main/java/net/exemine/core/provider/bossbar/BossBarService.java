package net.exemine.core.provider.bossbar;

import lombok.Getter;
import net.exemine.api.util.Executor;
import net.exemine.core.provider.BossBarProvider;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

public class BossBarService<T extends ExeUser<?>> {

    @Getter
    private static BossBarService<?> instance;

    public static BossBarService<?> get() {
        return instance;
    }

    private final UserService<T, ?> userService;
    private final BossBarProvider<T> provider;

    private ScheduledFuture<?> scheduledFuture;

    private final Map<UUID, BossBar<T>> bossBars = new HashMap<>();

    @Getter
    private long intervalInMillis = 500L;

    public BossBarService(JavaPlugin plugin, UserService<T, ?> userService, BossBarProvider<T> provider) {
        this.userService = userService;
        this.provider = provider;

        instance = this;
        BossBarListener<T> listener = new BossBarListener<T>(this, userService);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        schedule();
    }

    private void schedule() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        scheduledFuture = Executor
                .schedule(() -> {
                    userService.getOnlineUsers().forEach(user -> {
                        BossBar<T> bossBar = bossBars.get(user.getUniqueId());
                        if (bossBar == null) return;

                        bossBar.clear();
                        provider.update(user, bossBar);
                        bossBar.despawn();
                        bossBar.spawn();
                    });
                })
                .runAsyncTimer(0, intervalInMillis);
    }

    public void reschedule(long intervalInMillis) {
        this.intervalInMillis = intervalInMillis;
        schedule();
    }

    void setup(T user) {
        BossBar<T> bossBar = new BossBar<>(user);
        provider.setup(user, bossBar);
        bossBars.put(user.getUniqueId(), bossBar);
    }

    void destroy(T user) {
        bossBars.remove(user.getUniqueId());
    }
}
