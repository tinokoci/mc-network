package net.exemine.discord.ticket.impl.appeal.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.ticket.impl.appeal.AppealTicket;
import net.exemine.discord.ticket.impl.appeal.AppealTicketService;
import net.exemine.discord.ticket.impl.appeal.data.AppealData;
import net.exemine.discord.ticket.impl.appeal.stage.AppealStage;
import net.exemine.discord.ticket.state.TicketState;
import net.exemine.discord.user.NetworkUser;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AppealCommand extends BaseCommand {

    private final AppealTicketService appealTicketService;

    public AppealCommand(AppealTicketService appealTicketService) {
        super("appeal", "Opens a punishment appeal.");
        this.appealTicketService = appealTicketService;
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.deferReply(true).complete();

        NetworkUser networkUser = NetworkUser.getOrCreate(user);
        networkUser.loadBulkData();

        if (!MemberUtil.isLinked(user)) {
            hook.editOriginalEmbeds(EmbedUtil.getNotLinked()).queue();
            return;
        }
        if (networkUser.getBulkData().getActivePunishment(PunishmentType.BAN, PunishmentType.IP_BAN, PunishmentType.MUTE) == null) {
            hook.editOriginalEmbeds(EmbedUtil.error("You don't have a punishment that can be appealed."))
                    .queue();
            return;
        }
        AppealTicket ticket = appealTicketService.get(user);

        if (ticket != null) {
            TextChannel channel = ticket.getTextChannel();
            if (channel == null) {
                hook.editOriginalEmbeds(EmbedUtil.error("An error happened, please contact one of the developers."))
                        .queue();
                return;
            }
            hook.editOriginalEmbeds(EmbedUtil.error("You already have an appeal opened in " + channel.getAsMention() + '.'))
                    .queue();
            return;
        }
        ticket = appealTicketService.createTicket(Objects.requireNonNull(event.getMember()));

        TextChannel channel = ticket.getTextChannel();
        if (channel == null) return;

        hook.editOriginalEmbeds(EmbedUtil.create("New Appeal", "Your appeal has been opened in " + channel.getAsMention() + '.'))
                .queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        User user = event.getUser();
        AppealTicket ticket = appealTicketService.get(event.getChannel());
        if (ticket == null) return;

        if (ticket.getUser() != user) {
            event.replyEmbeds(EmbedUtil.error("You didn't open this punishment appeal."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        AppealData data = ticket.getData();

        if (componentId.equals(appealTicketService.getButtonGuiltyId())) {
            data.setGuilty(true);
        } else if (componentId.equals(appealTicketService.getButtonInnocentId())) {
            data.setGuilty(false);
        } else {
            data.setPunishmentType(PunishmentType.valueOf(componentId));
        }
        event.getMessage().delete().queue();
        appealTicketService.advanceStage(ticket);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User user = event.getAuthor();
        if (user.isBot()) return;

        MessageChannel channel = event.getChannel();
        AppealTicket ticket = appealTicketService.get(channel);
        if (ticket == null
                || ticket.getUser() != user
                || !ticket.isState(TicketState.CREATING))
            return;

        if (ticket.getStage() != AppealStage.REASON) {
            event.getMessage().delete().queue();
            return;
        }
        String message = event.getMessage().getContentRaw();
        ticket.getData().setReasonForPardon(message);

        channel.purgeMessages(channel.getHistory().retrievePast(20).complete());
        appealTicketService.advanceStage(ticket);
    }
}
