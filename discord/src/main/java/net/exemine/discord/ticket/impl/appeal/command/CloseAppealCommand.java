package net.exemine.discord.ticket.impl.appeal.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.punishment.Punishment;
import net.exemine.api.punishment.PunishmentService;
import net.exemine.api.punishment.shortened.ShortenedDuration;
import net.exemine.api.util.TimeUtil;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.ticket.impl.appeal.AppealTicket;
import net.exemine.discord.ticket.impl.appeal.AppealTicketService;
import net.exemine.discord.ticket.impl.appeal.result.AppealResult;
import net.exemine.discord.user.NetworkUser;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CloseAppealCommand extends BaseCommand {

    private final AppealTicketService appealTicketService;
    private final PunishmentService punishmentService;

    public CloseAppealCommand(AppealTicketService appealTicketService, PunishmentService punishmentService) {
        super("closeappeal", "Closes a punishment appeal in the channel the command is executed.", true);
        this.appealTicketService = appealTicketService;
        this.punishmentService = punishmentService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.STRING, "result", "Result of the appeal.", true)
                        .addChoices(Arrays.stream(AppealResult.values()).map(appealResult -> new Command.Choice(appealResult.name(), appealResult.name())).collect(Collectors.toList()))
        ));
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
        InteractionHook hook = event.deferReply().complete();
        NetworkUser networkUser = NetworkUser.getOrCreate(ticket.getUser(), NetworkUser::loadBulkData);
        Punishment punishment = networkUser.getBulkData().getPunishmentById(ticket.getData().getPunishmentId());
        AppealResult result = AppealResult.valueOf(Objects.requireNonNull(interaction.getOption("result")).getAsString());

        if (punishment == null) {
            result = AppealResult.DENIED;
        }
        appealTicketService.scheduleToClose(ticket);

        switch (result) {
            case DENIED:
                hook.editOriginalEmbeds(EmbedUtil.error("Appeal Denied", "This appeal will close in "
                                + DiscordUtil.code(TimeUtil.getNormalDuration(appealTicketService.getType().getClosingDuration())) + '.'))
                        .queue();
                break;
            case PARDONED:
                punishmentService.removePunishment(punishment, null, "Appeal Accepted");
                hook.editOriginalEmbeds(EmbedUtil.success("Appeal Accepted", "Your punishment has been removed.\nThis appeal will close in "
                                + DiscordUtil.code(TimeUtil.getNormalDuration(appealTicketService.getType().getClosingDuration())) + '.'))
                        .queue();
                break;
            case SHORTENED:
                long newDuration = ShortenedDuration.shortenDuration(punishment.getDuration());
                String message;

                if (newDuration == 0L) {
                    punishmentService.removePunishment(punishment, null, punishment.getAddedReason() + " (Shortened)");
                    message = "Your punishment has been removed.";
                } else {
                    punishment.setAddedReason(punishment.getAddedReason() + " (Shortened)");
                    punishment.setDuration(ShortenedDuration.shortenDuration(punishment.getDuration()));
                    punishmentService.updatePunishment(punishment);
                    message = "Your punishment has shortened to " + DiscordUtil.code(TimeUtil.getFullDuration(punishment.getDuration())) + '.';
                }
                hook.editOriginalEmbeds(EmbedUtil.success("Appeal Shortened", message
                                + "\nThis appeal will close in " + DiscordUtil.code(TimeUtil.getNormalDuration(appealTicketService.getType().getClosingDuration())) + '.'))
                        .queue();
                break;

        }
    }
}
