package net.exemine.discord.ticket.impl.support.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.exemine.api.util.TimeUtil;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.ticket.Ticket;
import net.exemine.discord.ticket.impl.support.SupportTicketService;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.Objects;

public class TicketCommand extends BaseCommand {

    private final SupportTicketService supportTicketService;

    public TicketCommand(SupportTicketService supportTicketService) {
        super("ticket", "Opens a support ticket.");
        this.supportTicketService = supportTicketService;
        setAsync(true);
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        Ticket ticket = supportTicketService.get(user);

        if (ticket != null) {
            TextChannel channel = ticket.getTextChannel();
            if (channel == null) {
                event.replyEmbeds(EmbedUtil.error("An error happened, please contact one of the developers."))
                        .setEphemeral(true)
                        .queue();
                return;
            }
            event.replyEmbeds(EmbedUtil.error(ticket.isOpen()
                            ? "You already have a ticket opened in " + channel.getAsMention() + '.'
                            : "You must wait " + DiscordUtil.code(TimeUtil.getNormalDuration(supportTicketService.getType().getClosingDuration())) + " between ticket creation."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        InteractionHook hook = event.deferReply(true).complete();

        ticket = supportTicketService.createTicket(Objects.requireNonNull(event.getMember()));
        TextChannel channel = ticket.getTextChannel();
        if (channel == null) return;
        hook.editOriginalEmbeds(EmbedUtil.create("New Ticket", "Your ticket has been opened in " + channel.getAsMention() + '.'))
                .queue();
    }
}