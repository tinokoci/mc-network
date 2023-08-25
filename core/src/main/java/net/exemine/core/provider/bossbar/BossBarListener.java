package net.exemine.core.provider.bossbar;

import lombok.RequiredArgsConstructor;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class BossBarListener<T extends ExeUser<?>> implements Listener {

    private final BossBarService<T> bossBarService;
    private final UserService<T, ?> userService;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        T user = userService.get(event.getPlayer());
        bossBarService.setup(user);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        T user = userService.get(event.getPlayer());
        if (user == null) return;
        bossBarService.destroy(user);
    }
}
