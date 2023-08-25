package net.exemine.discord.command.impl.staff;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.List;
import java.util.Objects;

public class ClearCommand extends BaseCommand {

    public ClearCommand() {
        super("clear", "Clear chat messages.", true);
        setOptionData(List.of(
                new OptionData(OptionType.INTEGER, "amount", "The amount of messages to delete", true)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        int amount = Objects.requireNonNull(interaction.getOption("amount")).getAsInt();

        if (amount > 100) {
            event.replyEmbeds(EmbedUtil.error("Maximum amount is " + DiscordUtil.code(100) + '.'))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        MessageChannel channel = event.getChannel();
        List<Message> messages = channel.getHistory().retrievePast(amount).complete();
        int actualAmount = messages.size();

        channel.purgeMessages(messages);
        event.replyEmbeds(EmbedUtil.success("Started deleting " + DiscordUtil.code(actualAmount) + " messages in this channel."))
                .setEphemeral(true)
                .queue();
    }
}