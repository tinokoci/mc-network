package net.exemine.uhc.world.antixray;

import com.execets.spigot.event.DiamondsNearAirDiscoveredEvent;
import lombok.RequiredArgsConstructor;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@RequiredArgsConstructor
public class AntiXrayListener implements Listener {

    private final AntiXrayService antiXrayService;
    private final WorldService worldService;

    @EventHandler
    public void onDiamondDiscovery(DiamondsNearAirDiscoveredEvent event) {
        if (worldService.isWorld(event.getWorld(), worldService.getUhcWorld())) {
            Location location = new Location(event.getWorld(), event.getX(), event.getY(), event.getZ());
            antiXrayService.addDiscoveredDiamond(location);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            antiXrayService.removeDiscoveredDiamond(event.getBlock().getLocation());
        }
    }
}
