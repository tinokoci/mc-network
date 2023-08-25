package net.exemine.discord.ticket.impl.application;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.Lang;
import net.exemine.discord.ticket.TicketService;
import net.exemine.discord.ticket.impl.application.stage.ApplicationStage;
import net.exemine.discord.ticket.state.TicketState;
import net.exemine.discord.ticket.type.TicketType;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.Arrays;
import java.util.EnumSet;

@Getter
public class ApplicationTicketService extends TicketService<ApplicationTicket> {

    private final String buttonContinueId = "continue";
    private final String buttonCloseId = "close";

    public ApplicationTicketService(JDA jda, DatabaseService databaseService) {
        super(jda, databaseService, TicketType.APPLICATION, ApplicationTicket.class, TicketState.CREATING, false);
    }

    @Override
    protected void sendInitialMessage(ApplicationTicket ticket, TextChannel channel, Member member) {
        channel.sendMessageEmbeds(EmbedUtil.builder()
                        .setTitle("Staff Application")
                        .setDescription(
                                "Hello " + member.getAsMention() + ", thanks for showing interest in becoming a staff member."
                                        + "\n" + DiscordConstants.INVISIBLE_CHAR
                                        + "\nYou're going to receive a set of questions that you're required to answer truthfully. "
                                        + "By continuing with this application you accept the consequences of not following this rule or not taking this application seriously which might result in an instant rejection and potential suspension from making future applications."
                                        + "\n" + DiscordConstants.INVISIBLE_CHAR
                                        + "\nWe're happy to hear from you and good luck!")
                        .setThumbnail(DiscordUtil.getGuild().getIconUrl())
                        .build())
                .setActionRows(ActionRow.of(
                        Button.success(buttonContinueId, "Continue"),
                        Button.danger(buttonCloseId, "Close")
                ))
                .queue();
    }

    @Override
    protected TextChannel createChannel(ApplicationTicket ticket, Category category, Member member) {
        EnumSet<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);

        return category.createTextChannel(getChannelName(member))
                .addPermissionOverride(member, permissions, null)
                .addPermissionOverride(DiscordConstants.getEveryoneRole(), null, permissions)
                .addPermissionOverride(DiscordUtil.getOrCreateRole(Rank.ADMIN), permissions, null)
                .addPermissionOverride(DiscordUtil.getOrCreateRole(Rank.DEVELOPER), permissions, null)
                .addPermissionOverride(DiscordUtil.getOrCreateRole(Rank.MANAGER), permissions, null)
                .complete();
    }

    public void advanceStage(ApplicationTicket ticket) {
        ApplicationStage newStage = Arrays.stream(ApplicationStage.values())
                .filter(stage -> stage.ordinal() - 1 == ticket.getStage().ordinal())
                .findFirst()
                .orElse(null);
        if (newStage == null) return;

        ticket.setStage(newStage);

        TextChannel channel = ticket.getTextChannel();
        if (channel == null) return;

        switch (newStage) {
            case AGE:
                channel.sendMessageEmbeds(EmbedUtil.create("How old are you?", "Please input a number."))
                        .queue();
                break;
            case TIME_ZONE:
                channel.sendMessageEmbeds(EmbedUtil.create("What is your time zone?", "Please input information in the short format (eg. UTC)."))
                        .queue();
                break;
            case COUNTRY:
                channel.sendMessageEmbeds(EmbedUtil.create("Which country are you from?", "Please refrain from providing us with a specific location."))
                        .queue();
                break;
            case LANGUAGES:
                channel.sendMessageEmbeds(EmbedUtil.create("Which languages can you speak fluently?", "Please don't include languages that you partially know."))
                        .queue();
                break;
            case AVAILABILITY:
                channel.sendMessageEmbeds(EmbedUtil.create("Please describe your time availability from Monday to Sunday.", "To help us understand your availability better, please let us know how available are you during the week."))
                        .queue();
                break;
            case HOST_ON_REDDIT:
                channel.sendMessageEmbeds(EmbedUtil.create("Are you currently a Trial Host on UHC.gg?", "If you have another position besides Trial Host please include it as well."))
                        .queue();
                break;
            case MINECRAFT_STAFF_EXPERIENCE:
                channel.sendMessageEmbeds(EmbedUtil.create("Do you have any past experience working on other Minecraft Servers/Projects?", "If you do, please list them below."))
                        .queue();
                break;
            case OTHER_STAFF_EXPERIENCE:
                channel.sendMessageEmbeds(EmbedUtil.create("Do you have any previous experience outside of Minecraft?", "If you do, please list them below."))
                        .queue();
                break;
            case QUALITIES:
                channel.sendMessageEmbeds(EmbedUtil.create("What qualities do you possess that make you a good fit for the UHC staff team?", "Tell us what makes you stand out from other applicants."))
                        .queue();
                break;
            case MOTIVATION:
                channel.sendMessageEmbeds(EmbedUtil.create("What motivates you to apply for a staff position on a UHC server?", "Tell us what you hope to achieve by joining the staff team."))
                        .queue();
                break;
            case FINISHED:
                channel.sendMessageEmbeds(EmbedUtil.success("Application Submitted",
                        "Your staff application is now under review."
                                + "\nA staff manager will get in touch with you as soon as possible."
                )).queue();
                ticket.setState(TicketState.OPEN);
        }
        updateTicketInDatabase(ticket);
    }
}
