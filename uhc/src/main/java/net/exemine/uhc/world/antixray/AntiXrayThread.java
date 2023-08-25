package net.exemine.uhc.world.antixray;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AntiXrayThread extends Thread {

    private static final long SLEEP = 5L;
    private static final long MAX_CHECKS_PER_TICK = 50_000L;
    private static final Vector HALF_BLOCK_OFFSET = new Vector(0.5, 0.5, 0.5);

    private final AntiXrayService antiXrayService;
    private final WorldService worldService;
    private final Set<Update> pendingUpdates = Collections.synchronizedSet(new HashSet<>());

    private int checks = 0;

    public AntiXrayThread(AntiXrayService antiXrayService, WorldService worldService) {
        this.antiXrayService = antiXrayService;
        this.worldService = worldService;

        setName("Anti Xray");
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (pendingUpdates) {
                    Iterator<Update> iterator = pendingUpdates.iterator();

                    while (iterator.hasNext() && checks++ < MAX_CHECKS_PER_TICK) {
                        Update update = iterator.next();
                        iterator.remove();

                        // Only update for alive & online users
                        if (!update.getUhcUser().isPlaying() || update.getUhcUser().getPlayer() == null) {
                            continue;
                        }
                        Player player = update.getUhcUser().getPlayer();
                        List<Location> targets = getClosestToPlayer(player.getLocation());

                        // We have nothing to check
                        if (targets.isEmpty()) {
                            continue;
                        }

                        for (Location target : targets) {
                            if (hasLineOfSight(player, target.toVector())) {
                                // Show diamonds
                                player.sendBlockChange(target, 56, (byte) 0);
                            }
                        }
                    }
                }
                Thread.sleep(SLEEP);
                checks = 0;
            }
        } catch (Exception e) {
            antiXrayService.resetThread();
            e.printStackTrace();
        }
    }

    public void queueUpdate(UHCUser uhcUser, Location location) {
        synchronized (pendingUpdates) {
            if (!uhcUser.isPlaying() || !worldService.isWorld(location.getWorld(), worldService.getUhcWorld())) {
                return;
            }
            Vector vector = location.toVector();
            if (uhcUser.getLastCheckedPosition() != null && vector.distanceSquared(uhcUser.getLastCheckedPosition()) < 1) {
                return;
            }
            uhcUser.setLastCheckedPosition(vector);
            pendingUpdates.add(new Update(uhcUser, vector));
        }
    }

    private boolean hasLineOfSight(Player player, Vector target) {
        Vector originClone = new Vector()
                .copy(player.getEyeLocation().toVector())
                .add(HALF_BLOCK_OFFSET);

        Vector targetClone = new Vector()
                .copy(target)
                .add(HALF_BLOCK_OFFSET);

        Vector direction = new Vector()
                .copy(targetClone)
                .subtract(originClone);

        double length = player.getEyeLocation().toVector().distance(target);
        BlockIterator iterator = new BlockIterator(
                player.getWorld(),
                player.getEyeLocation().toVector(),
                direction,
                0,
                NumberConversions.round(length));

        int solidBlocks = 0;
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (!block.getType().isTransparent() && !block.isLiquid()) {
                solidBlocks++;
            }
            if (solidBlocks > 5) {
                return false;
            }
        }
        return true;
    }

    private List<Location> getClosestToPlayer(Location location) {
        return new ArrayList<>(this.antiXrayService.getDiscoveredDiamonds())
                .stream()
                .filter(other -> other.distanceSquared(location) <= 1024)
                .collect(Collectors.toList());
    }

    @Data @AllArgsConstructor
    private static class Update {
        private UHCUser uhcUser;
        private Vector location;
    }
}
