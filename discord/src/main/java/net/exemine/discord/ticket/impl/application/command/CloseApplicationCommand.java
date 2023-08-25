package net.exemine.discord.ticket.impl.application.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.util.TimeUtil;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.ticket.impl.application.ApplicationTicket;
import net.exemine.discord.ticket.impl.application.ApplicationTicketService;
import net.exemine.discord.ticket.state.TicketState;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.List;
import java.util.Objects;

public class CloseApplicationCommand extends BaseCommand {

    private final ApplicationTicketService applicationTicketService;

    public CloseApplicationCommand(ApplicationTicketService applicationTicketService) {
        super("closeapplication", "Closes a staff application in the channel the command is executed.", true);
        this.applicationTicketService = applicationTicketService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.STRING, "status", "Status of the application.", true)
                        .addChoice("accepted", "ACCEPTED")
                        .addChoice("denied", "DENIED")
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        MessageChannel channel = event.getChannel();
        ApplicationTicket ticket = applicationTicketService.get(channel);

        if (ticket == null) {
            event.replyEmbeds(EmbedUtil.error("This channel is not marked as a staff application."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        boolean accepted = Objects.requireNonNull(interaction.getOption("status")).getAsString().equals("ACCEPTED");

        if (accepted) {
            applicationTicketService.close(ticket, TicketState.CLOSED);
        } else {
            applicationTicketService.scheduleToClose(ticket);
            event.replyEmbeds(EmbedUtil.error("Application Denied", "This application will close in "
                            + DiscordUtil.code(TimeUtil.getNormalDuration(applicationTicketService.getType().getClosingDuration())) + '.'))
                    .queue();
        }
    }
}
