package net.exemine.core.util;

import net.exemine.api.util.string.Lang;
import net.exemine.core.Core;
import net.exemine.core.user.CoreUser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class UserUtil {

    public static String getFormattedIssuer(UUID uuid) {
        if (uuid == null) {
            return Lang.CONSOLE_FORMAT;
        }
        if (ServerUtil.isServerThread()) {
            return Lang.NOT_ASYNC_THREAD;
        }
        Optional<CoreUser> user = Core.get().getUserService().fetch(uuid);
        return user.isPresent()
                ? user.get().getColoredRealName()
                : "Unknown";
    }

    public static UUID getJsonUUID(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return null;
        }
        return ((Player) sender).getUniqueId();
    }

    public static boolean isPlayerOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    public static boolean isPlayerOffline(UUID uuid) {
        return !isPlayerOnline(uuid);
    }

    public static boolean isPlayerOnline(String name) {
        return Bukkit.getPlayer(name) != null;
    }

    public static boolean isPlayerOffline(String name) {
        return !isPlayerOnline(name);
    }
}
