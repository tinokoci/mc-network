package net.exemine.uhc.config.impl;

import net.exemine.api.util.Executor;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AntiBurnListener extends ConfigListener {

    private static final long ANTI_BURN_TIMER = 30_000L;

    private final Map<ItemStack, UUID> droppedItems = new HashMap<>();

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        droppedItems.put(item, event.getPlayer().getUniqueId());
        Executor.schedule(() -> droppedItems.remove(item)).runSyncLater(ANTI_BURN_TIMER);
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        if (event.getEntity().getType() != EntityType.DROPPED_ITEM) return;

        ItemStack item = ((Item) event.getEntity()).getItemStack();
        UUID uuid = droppedItems.get(item);
        if (uuid == null) return;

        UHCUser user = plugin.getUserService().retrieve(uuid);
        List<ItemStack> burnedItems = user.getBurnedItems();

        burnedItems.add(item);
        Executor.schedule(() -> burnedItems.remove(item)).runSyncLater(ANTI_BURN_TIMER);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UHCUser user = plugin.getUserService().retrieve(event.getEntity().getUniqueId());

        List<ItemStack> burnedItems = user.getBurnedItems();
        event.getDrops().addAll(burnedItems);
        burnedItems.clear();
    }
}
