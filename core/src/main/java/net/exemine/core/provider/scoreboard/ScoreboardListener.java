package net.exemine.core.provider.scoreboard;

import lombok.RequiredArgsConstructor;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class ScoreboardListener<T extends ExeUser<?>> implements Listener {

    private final ScoreboardService<T> scoreboardService;
    private final UserService<T, ?> userService;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        T user = userService.get(event.getPlayer());
        scoreboardService.setup(user);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        T user = userService.get(event.getPlayer());
        if (user == null) return;
        scoreboardService.destroy(user);
    }
}
