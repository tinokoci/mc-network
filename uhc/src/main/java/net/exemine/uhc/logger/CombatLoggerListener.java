package net.exemine.uhc.logger;

import lombok.RequiredArgsConstructor;
import net.exemine.uhc.logger.event.CombatLoggerDeathEvent;
import net.exemine.uhc.logger.event.CombatLoggerSpawnEvent;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

@RequiredArgsConstructor
public class CombatLoggerListener implements Listener {

    private final CombatLoggerService combatLoggerService;
    private final UHCUserService userService;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.isCancelled() && ((CraftEntity) event.getEntity()).getHandle() instanceof CombatLoggerEntity) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onLoggerDeath(CombatLoggerDeathEvent event) {
        combatLoggerService.getLoggers().remove(event.getCombatLoggerEntity().getUniqueId());
    }

    @EventHandler
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        CombatLogger combatLogger = combatLoggerService.getLoggers().remove(player.getUniqueId());

        if (combatLogger != null) {
            CraftLivingEntity loggerEntity = combatLogger.getEntity().getBukkitEntity();

            event.setSpawnLocation(loggerEntity.getLocation());
            player.setFallDistance(loggerEntity.getFallDistance());
            player.setHealth(Math.min(player.getMaxHealth(), loggerEntity.getHealth()));
            player.setRemainingAir(loggerEntity.getRemainingAir());
            loggerEntity.remove();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UHCUser user = userService.get(event.getPlayer());
        if (!user.isPlaying()
                || user.isDead()
                || combatLoggerService.getLoggers().containsKey(user.getUniqueId()))
            return;

        Location location = user.getLocation();
        PlayerInventory inventory = player.getInventory();
        CombatLoggerEntity combatLoggerEntity = new CombatLoggerEntity(location.getWorld(), location, user, inventory.getContents(), inventory.getArmorContents());
        CombatLoggerSpawnEvent combatLoggerSpawnEvent = new CombatLoggerSpawnEvent(combatLoggerEntity);

        Bukkit.getPluginManager().callEvent(combatLoggerSpawnEvent);
        combatLoggerService.getLoggers().put(user.getUniqueId(), new CombatLogger(user.getUniqueId(), combatLoggerEntity, System.currentTimeMillis()));

        CraftLivingEntity craftLivingEntity = combatLoggerEntity.getBukkitEntity();
        if (craftLivingEntity != null) {
            EntityEquipment entityEquipment = craftLivingEntity.getEquipment();
            entityEquipment.setItemInHand(user.getInventory().getItemInHand());
            entityEquipment.setArmorContents(user.getInventory().getArmorContents());
            craftLivingEntity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteractEvent event) {
        if (combatLoggerService.getLoggers().values()
                .stream()
                .anyMatch(entry -> entry.getEntity().getBukkitEntity().equals(event.getEntity()))) {
            event.setCancelled(true);
        }
    }
}
