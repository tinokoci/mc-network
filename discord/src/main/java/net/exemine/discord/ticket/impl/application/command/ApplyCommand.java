package net.exemine.discord.ticket.impl.application.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.ticket.impl.application.ApplicationTicket;
import net.exemine.discord.ticket.impl.application.ApplicationTicketService;
import net.exemine.discord.ticket.impl.application.data.ApplicationData;
import net.exemine.discord.ticket.impl.application.stage.ApplicationStage;
import net.exemine.discord.ticket.state.TicketState;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ApplyCommand extends BaseCommand {

    private final ApplicationTicketService applicationTicketService;

    public ApplyCommand(ApplicationTicketService applicationTicketService) {
        super("apply", "Opens a staff application.");
        this.applicationTicketService = applicationTicketService;
        setAsync(true);
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.deferReply(true).complete();

        if (!MemberUtil.isLinked(user)) {
            hook.editOriginalEmbeds(EmbedUtil.getNotLinked()).queue();
            return;
        }
        ApplicationTicket ticket = applicationTicketService.get(user);

        if (ticket != null) {
            TextChannel channel = ticket.getTextChannel();
            if (channel == null) {
                hook.editOriginalEmbeds(EmbedUtil.error("An error happened, please contact one of the developers."))
                        .queue();
                return;
            }
            hook.editOriginalEmbeds(EmbedUtil.error("You already have an application opened in " + channel.getAsMention() + '.'))
                    .queue();
            return;
        }
        ticket = applicationTicketService.createTicket(Objects.requireNonNull(event.getMember()));

        TextChannel channel = ticket.getTextChannel();
        if (channel == null) return;

        hook.editOriginalEmbeds(EmbedUtil.create("New Application", "Your application has been opened in " + channel.getAsMention() + '.'))
                .queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        User user = event.getUser();
        ApplicationTicket ticket = applicationTicketService.get(event.getChannel());
        if (ticket == null) return;

        if (ticket.getUser() != user) {
            event.replyEmbeds(EmbedUtil.error("You didn't open this staff application."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (componentId.equals(applicationTicketService.getButtonCloseId())) {
            applicationTicketService.close(ticket, TicketState.INVALID);
            return;
        }
        if (componentId.equals(applicationTicketService.getButtonContinueId())) {
            ticket.getData().setAcceptedTerms(true);
            applicationTicketService.advanceStage(ticket);
            event.getMessage().delete().queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User user = event.getAuthor();
        if (user.isBot()) return;

        ApplicationTicket ticket = applicationTicketService.get(event.getChannel());
        if (ticket == null
                || ticket.getUser() != user
                || !ticket.isState(TicketState.CREATING)) return;

        ApplicationData data = ticket.getData();
        ApplicationStage stage = ticket.getStage();
        String message = event.getMessage().getContentRaw();

        switch (stage) {
            case TERMS:
                event.getMessage().delete().queue();
                return;
            case AGE:
                data.setAge(message);
                break;
            case COUNTRY:
                data.setCountry(message);
                break;
            case TIME_ZONE:
                data.setTimeZone(message);
                break;
            case LANGUAGES:
                data.setLanguages(message);
                break;
            case AVAILABILITY:
                data.setAvailability(message);
                break;
            case HOST_ON_REDDIT:
                data.setHostOnReddit(message);
                break;
            case MINECRAFT_STAFF_EXPERIENCE:
                data.setMinecraftStaffExperience(message);
                break;
            case OTHER_STAFF_EXPERIENCE:
                data.setOtherStaffExperience(message);
                break;
            case QUALITIES:
                data.setQualities(message);
                break;
            case MOTIVATION:
                data.setMotivation(message);
        }
        applicationTicketService.advanceStage(ticket);
    }
}