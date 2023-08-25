package net.exemine.discord.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

public class EmbedUtil {

    public static final Color RED = new Color(182, 0, 0);
    public static final Color GREEN = new Color(0, 169, 0);
    public static final Color PURPLE = new Color(170, 0, 170);

    public static EmbedBuilder builder(String title, String description) {
        return new EmbedBuilder()
                .setColor(PURPLE)
                .setTitle(title)
                .setDescription(description);
    }

    public static EmbedBuilder builder(boolean timestamp) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(PURPLE);

        if (timestamp) {
            builder.setFooter(DiscordUtil.getCurrentDate());
        }
        return builder;
    }

    public static EmbedBuilder builder() {
        return builder(false);
    }

    public static MessageEmbed create(Color color, String title, String description, String footer, String thumbnail) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description);

        if (color != null) {
            builder.setColor(color);
        }
        if (footer != null) {
            builder.setFooter(footer);
        }
        if (thumbnail != null) {
            builder.setThumbnail(thumbnail);
        }
        return builder.build();
    }

    public static MessageEmbed create(Color color, String title, String description, String footer) {
        return create(color, title, description, footer, null);
    }

    public static MessageEmbed create(String title, String description, String footer, String thumbnail) {
        return create(null, title, description, footer, thumbnail);
    }

    public static MessageEmbed create(String title, String description, String footer) {
        return create(PURPLE, title, description, footer);
    }

    public static MessageEmbed create(String title, String description) {
        return create(PURPLE, title, description, null);
    }

    public static MessageEmbed success(String title, String description) {
        return create(GREEN, title, description, null);
    }

    public static MessageEmbed success(String description) {
        return create(GREEN, "Success", description, null);
    }

    public static MessageEmbed error(String title, String description) {
        return create(RED, title, description, null);
    }

    public static MessageEmbed error(String description) {
        return create(RED, "Error", description, null);
    }

    public static MessageEmbed getUserNeverPlayed(String name) {
        return error("Minecraft account " + DiscordUtil.code(name) + " has never logged on the network.");
    }

    public static MessageEmbed getNotLinked() {
        return error("You're not linked to a minecraft account.");
    }
}
