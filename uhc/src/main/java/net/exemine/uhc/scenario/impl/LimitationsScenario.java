package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.string.CC;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LimitationsScenario extends ScenarioListener {

    private final Map<UUID, Integer> minedDiamond = new HashMap<>();
    private final Map<UUID, Integer> minedGold = new HashMap<>();
    private final Map<UUID, Integer> minedIron = new HashMap<>();

    private static final int DIAMOND_LIMIT = 16;
    private static final int GOLD_LIMIT = 32;
    private static final int IRON_LIMIT = 64;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        UHCUser user = plugin.getUserService().get(event.getPlayer());
        if (event.isCancelled() || !user.isPlaying()) return;

        UUID uuid = user.getUniqueId();
        Block block = event.getBlock();

        minedDiamond.putIfAbsent(uuid, 0);
        minedGold.putIfAbsent(uuid, 0);
        minedIron.putIfAbsent(uuid, 0);

        switch (block.getType()) {
            case DIAMOND_ORE:
                if (minedDiamond.get(uuid) >= DIAMOND_LIMIT) {
                    user.sendMessage(CC.RED + "You can't mine more than " + DIAMOND_LIMIT + " diamonds.");
                    event.setCancelled(true);
                } else {
                    minedDiamond.put(uuid, minedDiamond.get(uuid) + 1);
                }
                break;
            case GOLD_ORE:
                if (minedGold.get(uuid) >= GOLD_LIMIT) {
                    user.sendMessage(CC.RED + "You can't mine more than " + GOLD_LIMIT + " gold.");
                    event.setCancelled(true);
                } else {
                    minedGold.put(uuid, minedGold.get(uuid) + 1);
                }
                break;
            case IRON_ORE:
                if (minedIron.get(uuid) >= IRON_LIMIT) {
                    user.sendMessage(CC.RED + "You can't mine more than " + IRON_LIMIT + " iron.");
                    event.setCancelled(true);
                } else {
                    minedIron.put(uuid, minedIron.get(uuid) + 1);
                }
        }
    }
}
