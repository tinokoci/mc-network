package net.exemine.core.provider.nametag;

import lombok.RequiredArgsConstructor;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class NametagListener<T extends ExeUser<?>> implements Listener {

    private final NametagService<T> nametagService;
    private final UserService<T, ?> userService;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        T user = userService.get(event.getPlayer());
        nametagService.setup(user);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        T user = userService.get(event.getPlayer());
        if (user == null) return;
        nametagService.destroy(user);
    }
}
