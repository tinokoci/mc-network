package net.exemine.discord.ticket.impl.support.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.exemine.api.util.TimeUtil;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.ticket.Ticket;
import net.exemine.discord.ticket.impl.support.SupportTicketService;
import net.exemine.discord.ticket.state.TicketState;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

public class CloseTicketCommand extends BaseCommand {

    private final SupportTicketService supportTicketService;

    public CloseTicketCommand(SupportTicketService supportTicketService) {
        super("closeticket", "Closes a support ticket in the channel the command is executed.", true);
        this.supportTicketService = supportTicketService;
        setAsync(true);
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        MessageChannel channel = event.getChannel();
        Ticket ticket = supportTicketService.get(channel);

        if (ticket == null) {
            event.replyEmbeds(EmbedUtil.error("This channel is not marked as a support ticket."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (ticket.getState() == TicketState.CLOSING) {
            event.replyEmbeds(EmbedUtil.error("This ticket is already closing.")).queue();
            return;
        }
        supportTicketService.scheduleToClose(ticket);
        event.replyEmbeds(EmbedUtil.create("Ticket Update", "This ticket will close in "
                        + DiscordUtil.code(TimeUtil.getNormalDuration(supportTicketService.getType().getClosingDuration())) + '.'))
                .queue();
    }
}
