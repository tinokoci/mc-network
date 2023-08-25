package net.exemine.uhc.lobby;

import com.execets.spigot.ExeSpigot;
import net.exemine.uhc.location.LocationService;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.world.WorldService;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LobbyListener implements Listener {

    private final UHCUserService userService;
    private final WorldService worldService;

    public LobbyListener(UHCUserService userService, LocationService locationService, WorldService worldService) {
        this.userService = userService;
        this.worldService = worldService;
        ExeSpigot.INSTANCE.addMovementHandler(new LobbyMovementHandler(locationService, worldService));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        UHCUser user = userService.get(event.getPlayer());
        if (!user.isInLobby()) return;

        Action action = event.getAction();
        ItemStack itemInHand = user.getItemInHand();
        if (itemInHand == null) return;

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            switch (itemInHand.getType()) {
                case CAULDRON_ITEM:
                    user.performCommand("config");
                    break;
                case RAW_FISH:
                    user.performCommand("scenarios");
                    break;
                case IRON_SWORD:
                    user.performCommand("practice");
                    break;
                case FIREWORK:
                    user.performCommand("leaderboards");
                    break;
                case DOUBLE_PLANT:
                    user.performCommand("stats");
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        UHCUser user = userService.retrieve(entity.getUniqueId()); // retrieve because of combat logger
        if (user.isInLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        World world = event.getEntity().getWorld();

        if (worldService.isWorld(world, worldService.getLobbyWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        World world = event.getEntity().getWorld();

        if (worldService.isWorld(world, worldService.getLobbyWorld())) {
            event.setCancelled(true);
        }
    }
}
