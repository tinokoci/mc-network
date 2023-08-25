package net.exemine.core.user.base;

import lombok.RequiredArgsConstructor;
import net.exemine.api.util.string.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class ExeListener<T extends ExeUser<?>> implements Listener {

    private final UserService<T, ?> userService;

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        T user = userService.create(event.getUniqueId());
        user.loadData(false);
        user.onConnect(event);
        user.saveData(false);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        T user = userService.get(player);

        if (user == null) {
            player.kickPlayer(CC.RED + "Your data didn't load properly, report this to a developer.");
            return;
        }
        user.setup(player);
        user.onJoin();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        T user = userService.get(event.getPlayer());
        if (user != null) {
            user.onQuit();
        }
    }
}
