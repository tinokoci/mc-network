package net.exemine.uhc.assign;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

public class AutoAssignListener extends ConfigListener {

    @EventHandler
    public void onEntityDamageByEntityAssign(EntityDamageByEntityEvent event) {
        if (!plugin.getGameService().isAutoAssignRunning()) return;

        Entity victimEntity = event.getEntity();
        if (!(victimEntity instanceof Player)) return;
        Entity damagerEntity = event.getDamager();

        if (damagerEntity instanceof Player) {
            handleCheck((Player) victimEntity, (Player) damagerEntity, event);
        } else if (damagerEntity instanceof Projectile) {
            Projectile projectile = (Projectile) damagerEntity;
            ProjectileSource shooter = projectile.getShooter();

            if (!(shooter instanceof Player)) return;

            Player damager = (Player) shooter;
            if (damager == victimEntity) return;

            handleCheck((Player) victimEntity, damager, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR) // Run last
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.getGameService().isAutoAssignRunning()) return;

        UHCUser user = plugin.getUserService().retrieve(event.getPlayer());
        Team team = user.getTeam();
        Team assignedTeam = team.getAssignedTeam();

        if (assignedTeam == null) return;

        if (team.isDead()) {
            team.setAssignedTeam(null);
            assignedTeam.setAssignedTeam(null);

            List<Team> aliveTeams = plugin.getTeamService().getAliveTeams();
            if (aliveTeams
                    .stream()
                    .allMatch(aliveTeam -> aliveTeam.getAssignedTeam() == null)) {
                if (aliveTeams.size() == 1) return;
                new AutoAssignTask(plugin, false);
            }
        }
    }

    private void handleCheck(Player victimPlayer, Player damagerPlayer, Cancellable event) {
        UHCUser victim = plugin.getUserService().get(victimPlayer);
        UHCUser damager = plugin.getUserService().get(damagerPlayer);
        if (!victim.isPlaying() || !damager.isPlaying()) return;

        Team damagerAssignedTeam = damager.getTeam().getAssignedTeam();

        if (damagerAssignedTeam == null) {
            event.setCancelled(true);
            damagerPlayer.sendMessage(CC.RED + "You aren't assigned to anyone.");
            return;
        }
        Team victimTeam = victim.getTeam();

        if (damagerAssignedTeam != victimTeam) {
            event.setCancelled(true);
            damager.sendMessage(CC.PURPLE + "[Assign] " + damagerAssignedTeam.getLeader().getColoredDisplayName() + CC.GRAY
                    + (plugin.getGameService().isTeamGame() ? "'s team" : "") + " is assigned to you.");
        }
    }
}
