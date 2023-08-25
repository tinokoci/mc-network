package net.exemine.discord.changelog;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.exemine.api.util.string.Lang;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChangelogCommand extends BaseCommand {

    public ChangelogCommand() {
        super("changelog", "Starts the changelog creation process", true);
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        ChangeLog log = ChangeLog.get(user);

        if (log != null) {
            event.replyEmbeds(EmbedUtil.create("Changelog Process", "You already have an active changelog process."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        new ChangeLog(user);
        event.replyEmbeds(EmbedUtil.create("New Changelog Process",
                "You've started a new changelog process.\n" +
                        "\n" +
                        "Please input all new features, each in a separate message.\n" +
                        "When you finish, type `next` to continue or `cancel` to cancel the process.")
        ).queue();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        ChangeLog log = ChangeLog.get(event.getAuthor());
        MessageChannel channel = event.getChannel();
        String message = event.getMessage().getContentRaw();

        if (log == null) return;

        switch (message.toLowerCase()) {
            case "cancel":
                log.cancel();
                channel.sendMessageEmbeds(EmbedUtil.create("Changelog Process", "You've cancelled the changelog process.")).queue();
                return;
            case "next":
                if (log.isConfirmation()) return;
                log.advanceState();

                String description = log.isConfirmation() ?
                        "You've finished the process.\n"
                                + "\n"
                                + "Type `done` to send the changelog.\n "
                                + "Cancel the process by typing `cancel`." :

                        "Please input all " + log.getFormattedState() + ", each in a separate message.\n"
                                + "When you finish, type `next` to continue or `cancel` to cancel the process.";

                channel.sendMessageEmbeds(EmbedUtil.create("Changelog Process", description)).queue();
                return;
            case "done":
                EmbedBuilder embed = new EmbedBuilder();

                embed.setTitle("New " + Lang.SERVER_NAME + " Changelog");
                embed.setThumbnail("https://i.imgur.com/ILwZKs8.png");
                embed.setColor(EmbedUtil.PURPLE);
                embed.addField("New Features", generateString(log.getNewFeatures()), false);
                embed.addField("Tweaks", generateString(log.getTweaks()), false);
                embed.addField("Bug Fixes", generateString(log.getBugFixes()), false);
                embed.addField("Known Issues", generateString(log.getKnownIssues()), false);
                embed.addField("", "Please report all bugs and issues to help us keep the network running smoothly.\n" + DiscordConstants.INVISIBLE_CHAR, false);
                embed.setFooter(DiscordUtil.getCurrentDate());
                log.cancel();

                TextChannel changelogsChannel = DiscordConstants.getChannelChangeLogs();
                changelogsChannel.sendMessageEmbeds(embed.build()).queue();
                channel.sendMessageEmbeds(EmbedUtil.create("Changelog Process", "A new changelog has been sent to " + changelogsChannel.getAsMention() + '.'))
                        .queue();
                return;
        }
        switch (log.getState()) {
            case NEW_FEATURES:
                log.getNewFeatures().add(message);
                break;
            case TWEAKS:
                log.getTweaks().add(message);
                break;
            case BUG_FIXES:
                log.getBugFixes().add(message);
                break;
            case KNOWN_ISSUES:
                log.getKnownIssues().add(message);
        }
    }

    private String generateString(List<String> list) {
        StringBuilder builder = new StringBuilder();
        builder.append(DiscordConstants.INVISIBLE_CHAR).append("\n");

        if (list.isEmpty()) {
            builder.append("`-` None").append('\n');
        } else {
            list.forEach(string -> builder.append("`-` ").append(string).append('\n'));
        }
        builder.append(DiscordConstants.INVISIBLE_CHAR);
        return builder.toString();
    }
}
