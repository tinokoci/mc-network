package net.exemine.core.nms;

import com.execets.spigot.ExeSpigot;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.Executor;
import net.exemine.core.nms.hologram.Hologram;
import net.exemine.core.nms.npc.NPC;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class NMSListener implements Listener {

    private final UserService<CoreUser, CoreData> userService;

    public NMSListener(UserService<CoreUser, CoreData> userService) {
        this.userService = userService;
        ExeSpigot.INSTANCE.addPacketHandler(new NMSPacketHandler());
    }

    private void reloadNMS(Player player) {
        CoreUser user = userService.get(player);

        NPC.getNPCS().forEach(npc -> npc.spawn(user));
        Hologram.getHolograms().forEach(hologram -> {
            hologram.spawn(user);
            hologram.getBelow().forEach(below -> below.spawn(user));
            hologram.getAbove().forEach(above -> above.spawn(user));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        reloadNMS(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        CoreUser user = userService.get(event.getPlayer());
        if (user == null) return;
        NPC.getNPCS().forEach(npc -> npc.cleanup(user));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        reloadNMS(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Executor.schedule(() -> reloadNMS(event.getPlayer())).runSyncLater(500L);
    }
}