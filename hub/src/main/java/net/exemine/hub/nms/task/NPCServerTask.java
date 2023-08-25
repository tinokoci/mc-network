package net.exemine.hub.nms.task;

import net.exemine.api.instance.Instance;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.util.string.CC;
import net.exemine.core.nms.npc.NPC;
import net.exemine.hub.Hub;
import net.exemine.hub.nms.NMSService;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class NPCServerTask extends BukkitRunnable {

    private final NMSService nmsService;
    private final InstanceService instanceService;

    public NPCServerTask(Hub plugin, NMSService nmsService, InstanceService instanceService) {
        this.nmsService = nmsService;
        this.instanceService = instanceService;
        runTaskTimerAsynchronously(plugin, 20L, 20L);
    }

    @Override
    public void run() {
        rename(nmsService.getUhcNpc(), InstanceType.UHC);
        rename(nmsService.getFfaNpc(), InstanceType.FFA);
    }

    private void rename(NPC npc, InstanceType type) {
        if (npc == null) return;

        List<Instance> typeInstances = instanceService.getAllInstances(type);
        int players = typeInstances.stream().mapToInt(Instance::getOnlinePlayers).sum();

        boolean offline = typeInstances.isEmpty();

        npc.getAttachedHologram().getLineBelow(0).rename(CC.GRAY + (offline ? "N/A" : players) + " player" + (offline || players != 1 ? 's' : ""));
        npc.rename(offline ? CC.RED + "Not available!" : CC.GREEN + "Click to play!");
    }
}
