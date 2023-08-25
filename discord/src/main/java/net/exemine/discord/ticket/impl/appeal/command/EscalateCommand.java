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

public class EscalateCommand extends BaseCommand {

    private final AppealTicketService appealTicketService;

    public EscalateCommand(AppealTicketService appealTicketService) {
        super("escalate", "Escalates a ticket to higher staff members.", true);
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
        if (!appealTicketService.changeLevel(ticket, true)) {
            event.replyEmbeds(EmbedUtil.error("This ticket is already escalated to the highest level."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        event.reply(ticket.getLevel().getRanks()
                        .stream()
                        .map(rank -> DiscordUtil.getOrCreateRole(rank).getAsMention())
                        .collect(Collectors.joining(" ")))
                .addEmbeds(EmbedUtil.create("Ticket Escalated", "This ticket has been escalated to the " + ticket.getLevel().name().toLowerCase() + " level."))
                .queue();
    }
}