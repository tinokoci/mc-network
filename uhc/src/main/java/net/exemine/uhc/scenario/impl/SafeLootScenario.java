package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.uhc.UHC;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.scenario.loot.LootChest;
import net.exemine.uhc.scenario.loot.LootChestSpawnEvent;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SafeLootScenario extends ScenarioListener {
    
    private final UHC plugin = UHC.get();

    @EventHandler
    public void onLootChestSpawn(LootChestSpawnEvent event) {
        UHCUser killer = event.getUser().getGameKiller(false);
        if (killer == null) return;

        Set<UUID> killers = (plugin.getGameService().isTeamGame() && killer.getTeam() != null ?
                killer.getTeam().getMembers().stream()
                        .map(UHCUser::getUniqueId)
                        .collect(Collectors.toSet()) :
                Collections.singleton(killer.getUniqueId()));

        event.getLootChest().lock(killers);

        Executor.schedule(() -> event.getLootChest().unlock())
                .runSyncLater(30_000L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                event.getClickedBlock().getType().name().endsWith("CHEST")) {
            Chest chest = (Chest) event.getClickedBlock().getState();

            if (chest.hasMetadata(LootChest.LOCK_KEY) &&
                    chest.getMetadata(LootChest.LOCK_KEY).get(0).value() instanceof Set<?>) {
                Set<?> excluded = (Set<?>) chest.getMetadata(LootChest.LOCK_KEY).get(0).value();

                if (!excluded.contains(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(CC.RED + "This chest is protected.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChestBreak(BlockBreakEvent event) {
        if (event.getBlock().getType().name().endsWith("CHEST")) {
            Chest chest = (Chest) event.getBlock().getState();

            if (chest.hasMetadata(LootChest.LOCK_KEY) &&
                    chest.getMetadata(LootChest.LOCK_KEY).get(0).value() instanceof Set<?>) {
                Set<?> excluded = (Set<?>) chest.getMetadata(LootChest.LOCK_KEY).get(0).value();

                if (!excluded.contains(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(CC.RED + "This chest is protected.");
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> block.hasMetadata(LootChest.LOCK_KEY));
    }
}
