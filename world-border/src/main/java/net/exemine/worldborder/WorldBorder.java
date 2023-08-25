package net.exemine.worldborder;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldBorder extends JavaPlugin {

    public static WorldBorder plugin;

    public WorldBorder() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        // Load (or create new) config file
        Config.load(this);

        // our one real command, though it does also have aliases "wb" and "worldborder"
        getCommand("wborder").setExecutor(new WBCommand(this));

        // keep an eye on teleports, to redirect them to a spot inside the border if necessary
        getServer().getPluginManager().registerEvents(new WBListener(), this);

        // Well I for one find this info useful, so...
        Location spawn = getServer().getWorlds().get(0).getSpawnLocation();
        Config.log("For reference, the main world's spawn location is at X: " + Config.coord.format(spawn.getX()) + " Y: " + Config.coord.format(spawn.getY()) + " Z: " + Config.coord.format(spawn.getZ()));
    }

    @Override
    public void onDisable() {
        Config.StopBorderTimer();
        Config.StoreFillTask();
        Config.StopFillTask();
    }

    // for other plugins to hook into
    public BorderData GetWorldBorder(String worldName) {
        return Config.Border(worldName);
    }

    public static void teleport(final Player player, final Location location) {
        if (player.getVehicle() != null && player.getVehicle() instanceof Horse) {
            final Horse vehicle = ((Horse) player.getVehicle());
            vehicle.eject();

            // TP the horse
            new BukkitRunnable() {
                public void run() {
                    // Add 1 to location to be safe
                    vehicle.teleport(location.add(0, 1, 0));
                }
            }.runTaskLater(plugin, 1L);

            // Reattach the player to the horse
            new BukkitRunnable() {
                public void run() {
                    vehicle.setPassenger(player);
                }
            }.runTaskLater(plugin, 2L);

            //refresh player to fix invisibility glitch
            Bukkit.getServer().getScheduler().runTaskLater(WorldBorder.plugin, () -> updateEntity(player, getPlayersWithinViewDistance(player)),15); //run 15 ticks later
        } else {
            player.setFallDistance(0);
            player.teleport(location);
        }
    }

    private static void updateEntity(Entity entity, List<Player> observers) {

        World world = entity.getWorld();
        WorldServer worldServer = ((CraftWorld) world).getHandle();

        EntityTracker tracker = worldServer.tracker;
        EntityTrackerEntry entry = (EntityTrackerEntry) tracker.trackedEntities.get(entity.getEntityId());

        List<EntityPlayer> nmsPlayers = observers.stream().map(player -> ((CraftPlayer)player).getHandle()).collect(Collectors.toList());

        // Force Minecraft to resend packets to the affected clients
        entry.trackedPlayers.removeAll(nmsPlayers);
        entry.scanPlayers(new ArrayList<>(nmsPlayers));
    }

    private static List<Player> getPlayersWithinViewDistance(Player player) {
        List<Player> res = new ArrayList<>();
        int distance = Bukkit.getServer().getViewDistance() * 16;
        int d2 = distance * distance;

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getWorld() == player.getWorld()
                    && p.getLocation().distanceSquared(player.getLocation()) <= d2) {
                res.add(p);
            }
        }

        return res;
    }
}
