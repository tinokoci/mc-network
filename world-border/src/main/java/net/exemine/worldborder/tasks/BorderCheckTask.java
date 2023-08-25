package net.exemine.worldborder.tasks;

import net.exemine.worldborder.BorderData;
import net.exemine.worldborder.Config;
import net.exemine.worldborder.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class BorderCheckTask implements Runnable {
    public static Map<UUID, Location> lastValidLocationMap = new HashMap<>();

    @Override
    public void run()
    {
        // if knockback is set to 0, simply return
        if (Config.KnockBack() == 0.0)
            return;

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            checkPlayer(p, null, false, true);
        }

        //check combat loggers
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Zombie && entity.hasMetadata("CombatLogger")) {
                    BorderData borderData = Config.Border(entity.getWorld().getName());
                    if (borderData == null) return;
                    if (!borderData.insideBorder(entity.getLocation().getX(), entity.getLocation().getZ(), Config.ShapeRound())) {
                        int radius = borderData.getRadiusX();
                        double x;
                        double z;

                        if (entity.getLocation().getX() > radius)
                            x = radius - Config.KnockBack();
                        else
                            x = -radius + Config.KnockBack();

                        if (entity.getLocation().getZ() > radius)
                            z = radius - Config.KnockBack();
                        else
                            z = -radius + Config.KnockBack();

                        Location fixedLocation = new Location(entity.getWorld(), x, 100, z);
                        fixedLocation.setY(entity.getWorld().getHighestBlockYAt(fixedLocation) + 1);

                        entity.teleport(fixedLocation);
                    }
                }
            }
        }
    }

    // set targetLoc only if not current player location; set returnLocationOnly to true to have new Location returned if
    // they need to be moved to one, instead of directly handling it
    public static Location checkPlayer(Player player, Location targetLoc, boolean returnLocationOnly, boolean notify) {
        if (player == null) return null;

        Location loc = (targetLoc == null) ? player.getLocation().clone() : targetLoc;
        if (loc == null) return null;

        World world = loc.getWorld();
        if (world == null) return null;
        BorderData border = Config.Border(world.getName());
        if (border == null) return null;

        if (border.insideBorder(loc.getX(), loc.getZ(), Config.ShapeRound())) {
            lastValidLocationMap.put(player.getUniqueId(), player.getLocation());
            return null;
        }

        // if player is in bypass list (from bypass command), allow them beyond border; also ignore players currently being handled already
        if (Config.isPlayerBypassing(player.getName()))
            return null;

        Location newLoc = lastValidLocationMap.get(player.getUniqueId());

        // If the location doesn't exist or they are not in the border with their old location or they are changing worlds
        if (newLoc == null || !border.insideBorder(newLoc.getX(), newLoc.getZ(), Config.ShapeRound()) || !newLoc.getWorld().getName().equals(loc.getWorld().getName())) {
            newLoc = newLocation(player, loc, border, notify);
            lastValidLocationMap.remove(player.getUniqueId());
        }

        // Give some particle and sound effects where the player was beyond the border, if "whoosh effect" is enabled
        Config.showWhooshEffect(loc);

        if (!returnLocationOnly) {
            WorldBorder.teleport(player, newLoc);
        }

        if (returnLocationOnly)
            return newLoc;

        return null;
    }
    public static Location checkPlayer(Player player, Location targetLoc, boolean returnLocationOnly)
    {
        return checkPlayer(player, targetLoc, returnLocationOnly, true);
    }

    private static Location newLocation(Player player, Location loc, BorderData border, boolean notify)
    {
        if (Config.Debug())
        {
            Config.logWarn((notify ? "Border crossing" : "Check was run") + " in \"" + loc.getWorld().getName() + "\". Border " + border.toString());
            Config.logWarn("Player position X: " + Config.coord.format(loc.getX()) + " Y: " + Config.coord.format(loc.getY()) + " Z: " + Config.coord.format(loc.getZ()));
        }

        Location newLoc = border.correctedPosition(loc, Config.ShapeRound(), player.isFlying());

        // it's remotely possible (such as in the Nether) a suitable location isn't available, in which case...
        if (newLoc == null)
        {
            if (Config.Debug())
                Config.logWarn("Target new location unviable, using spawn or killing player.");
            if (Config.getIfPlayerKill())
            {
                player.setHealth(0.0D);
                return null;
            }

            Random random = new Random();
            int radius = WorldBorder.plugin.GetWorldBorder(loc.getWorld().getName()).getRadiusX();
            int randX = random.nextInt((radius * 2) + 1) + -radius;
            int randZ = random.nextInt((radius * 2) + 1) + -radius;

            loc = loc.getWorld().getHighestBlockAt(randX, randZ).getLocation();
            newLoc = newLocation(player, loc, border, false);
        }

        if (Config.Debug() && newLoc != null)
            Config.logWarn("New position in world \"" + newLoc.getWorld().getName() +
                    "\" at X: " + Config.coord.format(newLoc.getX()) + " Y: " + Config.coord.format(newLoc.getY()) + " Z: " + Config.coord.format(newLoc.getZ()));

        if (notify) {
            //player.sendMessage(Config.Message());
        }
        return newLoc;
    }

}