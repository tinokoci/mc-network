package net.exemine.api.util;

import net.exemine.api.util.spigot.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtil {

    private static final Logger LOGGER = Logger.getLogger(LogUtil.class.getName());
    private static final boolean IS_ANSI_SUPPORTED = JavaUtil.isAnsiSupported();

    public static void info(String message) {
        LOGGER.log(Level.INFO, colorize(message, ChatColor.RESET));
    }

    public static void warning(String message) {
        LOGGER.log(Level.WARNING, colorize(message, ChatColor.GOLD));
    }

    public static void error(String message) {
        LOGGER.log(Level.SEVERE, colorize(message, ChatColor.RED));
    }

    public static String colorize(String message, ChatColor defaultColor) {
        if (IS_ANSI_SUPPORTED) {
            message = defaultColor + message;
            for (ChatColor color : COLOR_REPLACEMENTS.keySet()) {
                message = message.replaceAll("(?i)" + color.toString(), COLOR_REPLACEMENTS.get(color));
            }
            return message + COLOR_REPLACEMENTS.get(ChatColor.RESET);
        } else {
            for (ChatColor color : COLOR_REPLACEMENTS.keySet()) {
                message = message.replaceAll("(?i)" + color.toString(), "");
            }
            return message;
        }
    }

    private static final Map<ChatColor, String> COLOR_REPLACEMENTS = new HashMap<ChatColor, String>() {{
        put(ChatColor.RESET, "\u001B[0m");
        put(ChatColor.BOLD, "\u001B[1m");
        put(ChatColor.ITALIC, "\u001B[3m");
        put(ChatColor.UNDERLINE, "\u001B[4m");
        put(ChatColor.STRIKETHROUGH, "\u001B[9m");
        put(ChatColor.BLACK, "\u001B[30m");
        put(ChatColor.DARK_GRAY, "\u001B[30m");
        put(ChatColor.DARK_RED, "\u001B[35m");
        put(ChatColor.RED, "\u001B[35m");
        put(ChatColor.DARK_GREEN, "\u001B[32m");
        put(ChatColor.GREEN, "\u001B[32m");
        put(ChatColor.YELLOW, "\u001B[33m");
        put(ChatColor.GOLD, "\u001B[33m");
        put(ChatColor.BLUE, "\u001B[34m");
        put(ChatColor.DARK_BLUE, "\u001B[34m");
        put(ChatColor.LIGHT_PURPLE, "\u001B[31m");
        put(ChatColor.DARK_PURPLE, "\u001B[31m");
        put(ChatColor.DARK_AQUA, "\u001B[36m");
        put(ChatColor.AQUA, "\u001B[36m");
        put(ChatColor.GRAY, "\u001B[37m");
        put(ChatColor.WHITE, "\u001B[37m");
    }};
}
