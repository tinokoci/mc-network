package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.core.nms.hologram.Hologram;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.scenario.loot.LootChestSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class TimeBombScenario extends ScenarioListener {

    @EventHandler
    public void onLootChestSpawn(LootChestSpawnEvent event) {
        Hologram hologram = new Hologram(CC.GREEN + "30",
                event.getLootChest().getLeftChest().getLocation().clone().add(0.5D, 1.0D, 0.0D));
        hologram.spawn();

        AtomicInteger countdown = new AtomicInteger(30);

        Executor.schedule(() -> {
            int time = countdown.decrementAndGet();

            String color = (time > 20 ? CC.GREEN : time > 10 ? CC.GOLD : CC.RED);
            hologram.rename(color + time);

            if (time == 0) {
                event.getLootChest().explode();
                hologram.destroy();
                Bukkit.broadcastMessage(CC.BOLD_GOLD + "[Time Bomb] " + event.getUser().getColoredDisplayName() + CC.GRAY + "'s corpse has exploded!");
            }
        }).runSyncTimer(1000L, 1000L);
    }
}
