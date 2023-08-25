package net.exemine.discord.util;

import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.discord.Discord;
import org.jetbrains.annotations.Nullable;

public class DiscordConstants {

    public static char INVISIBLE_CHAR = '\u200B';
    public static int TICKET_CATEGORIES_STARTING_POSITION = getConfig().getInt("ticket-categories-starting-position");

    // Roles
    public static Role getRoleMember() {
        return getRole("role.member");
    }

    public static Role getRoleLinked() {
        return getRole("role.linked");
    }

    public static Role getRoleLinkLock() {
        return getRole("role.link-lock");
    }

    public static Role getRoleUHCAlerts() {
        return getRole("role.uhc-alerts");
    }

    public static Role getEveryoneRole() {
        return DiscordUtil.getGuild().getPublicRole();
    }

    // Channels
    public static TextChannel getChannelBotCommands() {
        return getTextChannel("channel.bot-commands");
    }

    public static TextChannel getChannelChangeLogs() {
        return getTextChannel("channel.changelogs");
    }

    public static TextChannel getChannelStaffSuggestions() {
        return getTextChannel("channel.staff-suggestions");
    }

    public static TextChannel getChannelStaffList() {
        return getTextChannel("channel.staff-list");
    }

    public static TextChannel getChannelUHCApproval() {
        return getTextChannel("channel.uhc-approval");
    }

    public static TextChannel getChannelUpcomingMatches() {
        return getTextChannel("channel.upcoming-matches");
    }

    public static TextChannel getChannelWinners() {
        return getTextChannel("channel.winners");
    }

    public static NewsChannel getChannelMatchAlerts() {
        return getNewsChannel("channel.match-alerts");
    }

    // Emojis
    @Nullable
    public static Emoji getEmojiYes() {
        return getEmoji("emoji.agree");
    }

    @Nullable
    public static Emoji getEmojiNo() {
        return getEmoji("emoji.disagree");
    }

    // Utilities
    private static Role getRole(String path) {
        return DiscordUtil.getOrCreateRole(getConfig().getString(path));
    }

    private static TextChannel getTextChannel(String path) {
        return DiscordUtil.getOrCreateTextChannelByName(getConfig().getString(path));
    }

    private static NewsChannel getNewsChannel(String path) {
        return DiscordUtil.getOrCreateNewsChannelByName(getConfig().getString(path));
    }

    public static Emoji getEmoji(String path) {
        return DiscordUtil.getGuild().getEmojis()
                .stream()
                .filter(emoji -> emoji.getName().equalsIgnoreCase(getConfig().getString(path)))
                .findFirst()
                .orElse(null);
    }

    private static ConfigFile getConfig() {
        return Discord.get().getConfig();
    }
}
