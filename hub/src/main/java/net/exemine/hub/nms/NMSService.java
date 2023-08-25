package net.exemine.hub.nms;

import lombok.Getter;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.Core;
import net.exemine.core.nms.hologram.Hologram;
import net.exemine.core.nms.npc.NPC;
import net.exemine.hub.Hub;
import net.exemine.hub.location.LocationService;
import net.exemine.hub.nms.task.NPCServerTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public class NMSService {

    private final LocationService locationService;

    private NPC uhcNpc;
    private NPC ffaNpc;

    private Hologram welcomeHologram;

    public NMSService(Hub plugin, LocationService locationService) {
        this.locationService = locationService;
        setupNpcs();
        setupHolograms();
        new NPCServerTask(plugin, this, Core.get().getInstanceService());
    }

    public void setupNpcs() {
        Location uhcLocation = locationService.getUhcNpcLocation();

        if (uhcLocation != null) {
            uhcNpc = new NPC(CC.RED + "Not available!", "strongtino", uhcLocation, true)
                    .setItemInHand(new ItemStack(Material.GOLDEN_APPLE))
                    .attachHologram(new Hologram(CC.BOLD_PINK + "UHC").addLineBelow(CC.GRAY + "N/A players"));
        }
        Location ffaLocation = locationService.getFfaNpcLocation();

        if (ffaLocation != null) {
            ffaNpc = new NPC(CC.RED + "Not available!", "realvio", ffaLocation, true)
                    .setItemInHand(new ItemStack(Material.FISHING_ROD))
                    .attachHologram(new Hologram(CC.BOLD_PINK + "FFA").addLineBelow(CC.GRAY + "N/A players"));        }
    }

    public void setupHolograms() {
        Location welcomeLocation = locationService.getWelcomeHologramLocation();

        if (welcomeLocation != null) {
            welcomeHologram = new Hologram(CC.BOLD_PURPLE + Lang.SERVER_NAME + " Network", welcomeLocation)
                    .addLineBelow("")
                    .addLineBelow(CC.GRAY + "News, UHC Matches & More: ")
                    .addLineBelow(CC.UNDERLINE_PINK + Lang.DISCORD);
        }
    }
}
