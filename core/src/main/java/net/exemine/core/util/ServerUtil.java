package net.exemine.core.util;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class ServerUtil {

    public static void performCommand(CommandSender sender, String command) {
        if (sender instanceof Player) {
            ((Player) sender).performCommand(command);
        } else {
            performCommand(command);
        }
    }

    public static void performCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public static void launchFirework(Location location, Color color, int power, boolean trail, boolean flicker) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(color).trail(trail).flicker(flicker).build());
        meta.setPower(power);
        firework.setFireworkMeta(meta);
    }

    public static int getMenuRowsByElements(int count) {
        return Math.max(1, count / 9 + (count % 9 == 0 ? 0 : 1));
    }

    public static boolean isServerThread() {
        return Thread.currentThread() == getServerThread();
    }

    public static Thread getServerThread() {
        return ((CraftServer) Bukkit.getServer()).getServer().primaryThread;
    }
}
