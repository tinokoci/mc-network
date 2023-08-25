package net.exemine.uhc.world.antixray;

import com.execets.spigot.ExeSpigot;
import lombok.Getter;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AntiXrayService {

    private final WorldService worldService;

    private final List<Location> discoveredDiamonds = new ArrayList<>();
    private AntiXrayThread thread;

    public AntiXrayService(UHCUserService uhcUserService, WorldService worldService) {
        this.worldService = worldService;

        resetThread();

        ExeSpigot.INSTANCE.addMovementHandler(new AntiXrayMoveHandler(this, uhcUserService));
    }

    public void addDiscoveredDiamond(Location location) {
        if (!discoveredDiamonds.contains(location)) {
            discoveredDiamonds.add(location);
        }
    }

    public void removeDiscoveredDiamond(Location location) {
        discoveredDiamonds.remove(location);
    }

    public void resetThread() {
        thread = new AntiXrayThread(this, worldService);
        thread.start();
    }
}
