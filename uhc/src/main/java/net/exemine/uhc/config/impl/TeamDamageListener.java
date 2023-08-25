package net.exemine.uhc.config.impl;

import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TeamDamageListener extends ConfigListener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (plugin.getGameService().isSoloGame()
                || plugin.getGameService().isStateOrLower(GameState.SCATTERING))
            return;

        Entity victimEntity = event.getEntity();
        Entity damagerEntity = event.getDamager();

        if (victimEntity instanceof Player && damagerEntity instanceof Player) {
            UHCUser victim = plugin.getUserService().get(victimEntity.getUniqueId());
            UHCUser damager = plugin.getUserService().get(damagerEntity.getUniqueId());

            Team team = victim.getTeam();

            if (team != null && team.hasMember(damager)) {
                event.setCancelled(true);
            }
        }
    }
}
