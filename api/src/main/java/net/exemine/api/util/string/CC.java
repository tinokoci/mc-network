package net.exemine.api.util.string;

import net.exemine.api.util.spigot.ChatColor;

public class CC {

    public static final String DARK_BLUE = ChatColor.DARK_BLUE.toString();
    public static final String DARK_GREEN = ChatColor.DARK_GREEN.toString();
    public static final String DARK_AQUA = ChatColor.DARK_AQUA.toString();
    public static final String DARK_RED = ChatColor.DARK_RED.toString();
    public static final String PURPLE = ChatColor.DARK_PURPLE.toString();
    public static final String GOLD = ChatColor.GOLD.toString();
    public static final String GRAY = ChatColor.GRAY.toString();
    public static final String DARK_GRAY = ChatColor.DARK_GRAY.toString();
    public static final String BLUE = ChatColor.BLUE.toString();
    public static final String GREEN = ChatColor.GREEN.toString();
    public static final String AQUA = ChatColor.AQUA.toString();
    public static final String RED = ChatColor.RED.toString();
    public static final String PINK = ChatColor.LIGHT_PURPLE.toString();
    public static final String YELLOW = ChatColor.YELLOW.toString();
    public static final String WHITE = ChatColor.WHITE.toString();

    public static final String RESET = ChatColor.RESET.toString();
    public static final String ITALIC = ChatColor.ITALIC.toString();
    public static final String BOLD = ChatColor.BOLD.toString();
    public static final String UNDERLINE = ChatColor.UNDERLINE.toString();
    public static final String STRIKETHROUGH = ChatColor.STRIKETHROUGH.toString();
    public static final String OBFUSCATED = ChatColor.MAGIC.toString();

    public static final String BOLD_DARK_GREEN = DARK_GREEN + BOLD;
    public static final String BOLD_DARK_AQUA = DARK_AQUA + BOLD;
    public static final String BOLD_DARK_RED = DARK_RED + BOLD;
    public static final String BOLD_PURPLE = PURPLE + BOLD;
    public static final String BOLD_GOLD = GOLD + BOLD;
    public static final String BOLD_GRAY = GRAY + BOLD;
    public static final String BOLD_DARK_GRAY = DARK_GRAY + BOLD;
    public static final String BOLD_BLUE = BLUE + BOLD;
    public static final String BOLD_GREEN = GREEN + BOLD;
    public static final String BOLD_AQUA = AQUA + BOLD;
    public static final String BOLD_RED = RED + BOLD;
    public static final String BOLD_PINK = PINK + BOLD;
    public static final String BOLD_YELLOW = YELLOW + BOLD;
    public static final String BOLD_WHITE = WHITE + BOLD;

    public static final String ITALIC_DARK_GREEN = DARK_GREEN + ITALIC;
    public static final String ITALIC_DARK_AQUA = DARK_AQUA + ITALIC;
    public static final String ITALIC_DARK_RED = DARK_RED + ITALIC;
    public static final String ITALIC_PURPLE = PURPLE + ITALIC;
    public static final String ITALIC_GOLD = GOLD + ITALIC;
    public static final String ITALIC_GRAY = GRAY + ITALIC;
    public static final String ITALIC_DARK_GRAY = DARK_GRAY + ITALIC;
    public static final String ITALIC_BLUE = BLUE + ITALIC;
    public static final String ITALIC_GREEN = GREEN + ITALIC;
    public static final String ITALIC_AQUA = AQUA + ITALIC;
    public static final String ITALIC_RED = RED + ITALIC;
    public static final String ITALIC_PINK = PINK + ITALIC;
    public static final String ITALIC_YELLOW = YELLOW + ITALIC;
    public static final String ITALIC_WHITE = WHITE + ITALIC;

    public static final String STRIKETHROUGH_DARK_GREEN = DARK_GREEN + STRIKETHROUGH;
    public static final String STRIKETHROUGH_DARK_AQUA = DARK_AQUA + STRIKETHROUGH;
    public static final String STRIKETHROUGH_DARK_RED = DARK_RED + STRIKETHROUGH;
    public static final String STRIKETHROUGH_PURPLE = PURPLE + STRIKETHROUGH;
    public static final String STRIKETHROUGH_GOLD = GOLD + STRIKETHROUGH;
    public static final String STRIKETHROUGH_GRAY = GRAY + STRIKETHROUGH;
    public static final String STRIKETHROUGH_DARK_GRAY = DARK_GRAY + STRIKETHROUGH;
    public static final String STRIKETHROUGH_BLUE = BLUE + STRIKETHROUGH;
    public static final String STRIKETHROUGH_GREEN = GREEN + STRIKETHROUGH;
    public static final String STRIKETHROUGH_AQUA = AQUA + STRIKETHROUGH;
    public static final String STRIKETHROUGH_RED = RED + STRIKETHROUGH;
    public static final String STRIKETHROUGH_PINK = PINK + STRIKETHROUGH;
    public static final String STRIKETHROUGH_YELLOW = YELLOW + STRIKETHROUGH;
    public static final String STRIKETHROUGH_WHITE = WHITE + STRIKETHROUGH;

    public static final String UNDERLINE_DARK_GREEN = DARK_GREEN + UNDERLINE;
    public static final String UNDERLINE_DARK_AQUA = DARK_AQUA + UNDERLINE;
    public static final String UNDERLINE_DARK_RED = DARK_RED + UNDERLINE;
    public static final String UNDERLINE_PURPLE = PURPLE + UNDERLINE;
    public static final String UNDERLINE_GOLD = GOLD + UNDERLINE;
    public static final String UNDERLINE_GRAY = GRAY + UNDERLINE;
    public static final String UNDERLINE_DARK_GRAY = DARK_GRAY + UNDERLINE;
    public static final String UNDERLINE_BLUE = BLUE + UNDERLINE;
    public static final String UNDERLINE_GREEN = GREEN + UNDERLINE;
    public static final String UNDERLINE_AQUA = AQUA + UNDERLINE;
    public static final String UNDERLINE_RED = RED + UNDERLINE;
    public static final String UNDERLINE_PINK = PINK + UNDERLINE;
    public static final String UNDERLINE_YELLOW = YELLOW + UNDERLINE;
    public static final String UNDERLINE_WHITE = WHITE + UNDERLINE;

    public static String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
