package net.exemine.discord.ticket.impl.appeal.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.ticket.impl.appeal.AppealTicket;
import net.exemine.discord.ticket.impl.appeal.AppealTicketService;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.stream.Collectors;

public class DeescalateCommand extends BaseCommand {

    private final AppealTicketService appealTicketService;

    public DeescalateCommand(AppealTicketService appealTicketService) {
        super("deescalate", "Deescalates a ticket to lower staff members.", true);
        this.appealTicketService = appealTicketService;
    }


    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        MessageChannel channel = event.getChannel();
        AppealTicket ticket = appealTicketService.get(channel);

        if (ticket == null) {
            event.replyEmbeds(EmbedUtil.error("This channel is not marked as a punishment appeal."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (!appealTicketService.changeLevel(ticket, false)) {
            event.replyEmbeds(EmbedUtil.error("This ticket is already deescalated to the lowest level."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        event.reply(ticket.getLevel().getRanks()
                        .stream()
                        .map(rank -> DiscordUtil.getOrCreateRole(rank).getAsMention())
                        .collect(Collectors.joining(" ")))
                .addEmbeds(EmbedUtil.create("Ticket Deescalated", "This ticket has been deescalated to the " + ticket.getLevel().name().toLowerCase() + " level."))
                .queue();
    }
}