package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.Cooldown;
import net.exemine.api.util.string.CC;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.info.DoNotDisturbInfo;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.stream.Collectors;

public class DoNotDisturbScenario extends ScenarioListener {

    private final Cooldown<DoNotDisturbInfo> cooldown = DoNotDisturbInfo.COOLDOWN;

    @Override
    protected void onDisable() {
        plugin.getUserService().values().forEach(user -> {
            Team team = user.getTeam();
            if (team == null) return;
            DoNotDisturbInfo info = team.getDoNotDisturbInfo();
            cooldown.remove(info);
            info.setEnemy(null);
        });
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        Entity entityDamager = event.getDamager();
        Player playerDamager = null;

        if (entityDamager instanceof Player) {
            playerDamager = (Player) entityDamager;
        } else if (entityDamager instanceof Arrow) {
            if (!(((Arrow) entityDamager).getShooter() instanceof Player)) return;
            playerDamager = (Player) ((Arrow) entityDamager).getShooter();
        } else if (entityDamager instanceof FishHook) {
            if (!(((FishHook) entityDamager).getShooter() instanceof Player)) return;
            playerDamager = (Player) ((FishHook) entityDamager).getShooter();
        }
        if (playerDamager == null || playerDamager == entity) return;

        UHCUser victim = plugin.getUserService().get((Player) entity);
        UHCUser damager = plugin.getUserService().get(playerDamager);
        Team victimTeam = victim.getTeam();
        Team damagerTeam = damager.getTeam();

        if (victimTeam.equals(damagerTeam) || victimTeam.isCrossTeamingWith(damagerTeam)) return;

        DoNotDisturbInfo victimInfo = victimTeam.getDoNotDisturbInfo();
        DoNotDisturbInfo damagerInfo = damagerTeam.getDoNotDisturbInfo();

        if (victim.isPlaying() && (damager.isGameModerator())) {
            if (victimInfo.isActive() && !victimInfo.getEnemy().getAllAliveMembers().isEmpty()) {
                damager.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "This player is linked to " +
                        victimInfo.getEnemy().getAllAliveMembers().stream()
                                .map(UHCUser::getColoredDisplayName)
                                .collect(Collectors.joining(CC.GRAY + ", ")) + CC.GRAY
                        + " for " + CC.GOLD + cooldown.getNormalDuration(victimInfo) + CC.GRAY + '.');
            } else {
                damager.sendMessage(CC.RED + "This player is not linked to anyone.");
            }
        }
        if (!victim.isPlaying() || !damager.isPlaying()) return;

        // Important: Set enemy to null again if they're not linked anymore
        if (!damagerInfo.isActive() && damagerInfo.getEnemy() != null) {
            damagerInfo.setEnemy(null);
            cooldown.remove(damagerInfo);
        }
        if (!victimInfo.isActive() && victimInfo.getEnemy() != null) {
            victimInfo.setEnemy(victimTeam);
            cooldown.remove(victimInfo);
        }

        // None of the players are linked
        if (!damagerInfo.isActive() && !victimInfo.isActive() && !(damager instanceof FishHook)) {
            victimInfo.setEnemy(damagerTeam);
            damagerInfo.setEnemy(victimTeam);
            cooldown.put(victimInfo, 30);
            cooldown.put(damagerInfo, 30);

            victimTeam.getAllAliveMembers().forEach(member ->
                    member.sendMessage(CC.PURPLE + "[DnD] " + CC.GRAY + "You're now linked to " + damagerTeam.getAllAliveMembers().stream()
                            .map(UHCUser::getColoredDisplayName)
                            .collect(Collectors.joining(CC.GRAY + ", ")) + CC.GRAY + '.'));
            damagerTeam.getAllAliveMembers().forEach(member ->
                    member.sendMessage(CC.PURPLE + "[DnD] " + CC.GRAY + "You're now linked to " + victimTeam.getAllAliveMembers().stream()
                            .map(UHCUser::getColoredDisplayName)
                            .collect(Collectors.joining(CC.GRAY + ", ")) + CC.GRAY + '.'));
        }
        // Linked players hit each other
        else if (victimTeam.equals(damagerInfo.getEnemy()) || victimTeam.isCrossTeamingWith(damagerInfo.getEnemy()) ||
                damagerTeam.equals(victimInfo.getEnemy()) || damagerTeam.isCrossTeamingWith(victimInfo.getEnemy())) {
            cooldown.put(victimInfo, 30);
            cooldown.put(damagerInfo, 30);
        }
        // Someone else tried to interfere
        else {
            damager.sendMessage(CC.RED + "You are not linked to that player.");
            event.setCancelled(true);
        }
    }
}
