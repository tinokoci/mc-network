package net.exemine.discord.util;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.exemine.api.rank.Rank;
import net.exemine.discord.Discord;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

public class DiscordUtil {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy - HH:mm") {{
        setTimeZone(TimeZone.getTimeZone("UTC"));
    }};

    public static String getCurrentDate() {
        return DATE_FORMAT.format(new Date());
    }

    public static TextChannel getTextChannelById(String id) {
        return getGuild().getTextChannelById(id);
    }

    public static TextChannel getOrCreateTextChannelByName(String channelName) {
        return getTextChannelByName(channelName).orElseGet(() -> getGuild().createTextChannel(channelName).complete());
    }

    public static Optional<TextChannel> getTextChannelByName(String channelName) {
        return getGuild().getTextChannelsByName(channelName, true)
                .stream()
                .findFirst();
    }

    public static NewsChannel getOrCreateNewsChannelByName(String channelName) {
        return getNewsChannelByName(channelName).orElseGet(() -> getGuild().createNewsChannel(channelName).complete());
    }

    public static Optional<NewsChannel> getNewsChannelByName(String channelName) {
        return getGuild().getNewsChannelsByName(channelName, true)
                .stream()
                .findFirst();
    }

    public static boolean hasCategory(String name) {
        return getCategory(name) != null;
    }

    public static Category getCategory(String name) {
        return getGuild().getCategoriesByName(name, true)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static Role getRole(String roleName) {
        return getGuild().getRolesByName(roleName, true)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public static Role getOrCreateRole(String roleName) {
        Role role = getRole(roleName);

        if (role == null) {
            role = getGuild()
                    .createRole()
                    .setName(roleName)
                    .complete();
        }
        return role;
    }

    public static Role getOrCreateRole(Rank rank) {
        return getOrCreateRole(rank.getDiscordRole());
    }

    public static User getUser(String userId) {
        return Discord.get().getJda().getUserById(userId);
    }

    @NotNull
    public static Activity getActivity(String activityTypeName, String name) {
        return Activity.of(
                Arrays.stream(Activity.ActivityType.values())
                        .filter(type -> type.name().equalsIgnoreCase(activityTypeName))
                        .findFirst()
                        .orElse(Activity.ActivityType.PLAYING),
                name
        );
    }

    public static void refreshCategoryPositions() {
        getGuild().modifyCategoryPositions().queue();
    }

    public static Guild getGuild() {
        return Discord.get().getGuild();
    }

    public static String code(Object text) {
        return "`" + text + "`";
    }

    public static String bold(Object object) {
        return "**" + object + "**";
    }

    public static String underline(Object object) {
        return "__" + object + "__";
    }

    public static String boldUnderline(Object object) {
        return bold(underline(object));
    }
}
