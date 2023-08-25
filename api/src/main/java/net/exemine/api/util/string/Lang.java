package net.exemine.api.util.string;

public class Lang {

    public static final String SERVER_NAME = "Exemine";
    public static final String DOMAIN = "exemine.net";
    public static final String SERVER_IP = "mc." + DOMAIN;
    public static final String WEBSITE = "www." + DOMAIN;
    public static final String STORE = "store." + DOMAIN;
    public static final String DISCORD = "discord." + DOMAIN;
    public static final String TWITTER = "@ExemineNET";
    public static final String UHC_FEED_TWITTER = "@ExemineFeed";

    public static final char BULLET = '●';
    public static final char LINE = '┃';
    public static final char HEART = '❤';
    public static final char COIN = '⛁';
    public static final char CHECKMARK = '\u2714';
    public static final char X = '\u2718';
    public static final char ARROW_RIGHT = '➟';
    public static final char ARROW_UP = '⬆';
    public static final char ARROW_DOWN = '⬇';

    public static final String NOT_ASYNC_THREAD = "Thread issue (NAT)";
    public static final String CONSOLE_FORMAT = CC.BOLD_RED + "Console";
    public static final String LIST_PREFIX = ' ' + CC.GRAY + Lang.BULLET + ' ';

    public static final String NO_PERMISSION = CC.RED + "You don't have permission for that command.";
    public static final String USER_NOT_FOUND = CC.RED + "That player cannot be found.";
    public static final String USER_NEVER_PLAYED = CC.RED + "That player has never played on here.";
    public static final String REPORT_TO_DEV = CC.RED + "An issue has occurred, please report this to a developer.";

    private static final String UHC_WRONG_GAME_STATE = CC.RED + "You cannot do that in the current game state.";
    private static final String UHC_WRONG_USER_STATE = CC.RED + "You cannot do that in your current state.";
}
