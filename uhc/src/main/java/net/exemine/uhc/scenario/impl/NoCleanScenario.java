package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.Cooldown;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.info.NoCleanInfo;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class NoCleanScenario extends ScenarioListener {

    private final Cooldown<UUID> cooldown = NoCleanInfo.COOLDOWN;

    @Override
    protected void onDisable() {
        plugin.getUserService().values().forEach(user -> cooldown.remove(user.getUniqueId()));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        if (killer != null) {
            UHCUser killerUser = plugin.getUserService().get(killer);
            if (!killerUser.isPlaying()) return;
            addNoClean(killerUser);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof Player && cooldown.isActive(damager.getUniqueId())) {
            removeNoClean(plugin.getUserService().get((Player) damager));
            return;
        }
        if (damager instanceof Arrow
                && ((Arrow) damager).getShooter() instanceof Player
                && cooldown.isActive(((Player) ((Arrow) damager).getShooter()).getUniqueId())) {
            removeNoClean(plugin.getUserService().get((Player) ((Arrow) damager).getShooter()));
            return;
        }
        if (damager instanceof FishHook
                && ((FishHook) damager).getShooter() instanceof Player
                && cooldown.isActive(((Player) ((FishHook) damager).getShooter()).getUniqueId())) {
            removeNoClean(plugin.getUserService().get((Player) ((FishHook) damager).getShooter()));
            return;
        }
        if (damager instanceof Player && event.getEntity() instanceof Player) {
            if (cooldown.isActive(event.getEntity().getUniqueId())) {
                damager.sendMessage(CC.RED + "[No Clean] That player has an invincibility timer.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player && cooldown.isActive(entity.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();

        if (player != null && cooldown.isActive(player.getUniqueId())) {
            removeNoClean(plugin.getUserService().get(player));
        }
    }

    private void addNoClean(UHCUser user) {
        cooldown.put(user.getUniqueId(), 30);
        user.sendMessage(CC.GREEN + "[No Clean] You're now invincible for 30 seconds.");

        Executor.schedule(() -> removeNoClean(user)).runSyncLater(30_000L);
    }

    private void removeNoClean(UHCUser user) {
        if (!cooldown.contains(user.getUniqueId())) return;
        cooldown.remove(user.getUniqueId());
        user.sendMessage(CC.RED + "[No Clean] You're no longer invincible.");
    }
}
