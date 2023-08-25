package net.exemine.discord.ticket.impl.support;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.rank.Rank;
import net.exemine.discord.ticket.Ticket;
import net.exemine.discord.ticket.TicketService;
import net.exemine.discord.ticket.state.TicketState;
import net.exemine.discord.ticket.type.TicketType;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;

import java.util.Arrays;
import java.util.EnumSet;

public class SupportTicketService extends TicketService<Ticket> {

    public SupportTicketService(JDA jda, DatabaseService databaseService) {
        super(jda, databaseService, TicketType.SUPPORT, Ticket.class, TicketState.OPEN, true);
    }

    protected void sendInitialMessage(Ticket ticket, TextChannel channel, Member member) {
        String optionalMinecraftName = MemberUtil.isLinked(member) ? "" : "\nPlease input your IGN if you need server assistance.";
        channel.sendMessageEmbeds(EmbedUtil.create("Support Ticket", "Hello " + member.getAsMention() + ", thanks for creating a ticket."
                + "\nA staff member will get in touch with you shortly."
                + optionalMinecraftName)
        ).queue();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected TextChannel createChannel(Ticket ticket, Category category, Member member) {
        EnumSet<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
        ChannelAction<TextChannel> action = category.createTextChannel(getChannelName(member));
        Arrays.stream(Rank.values())
                .filter(rank -> rank.isEqualOrAbove(Rank.MOD))
                .forEach(rank -> action.addPermissionOverride(DiscordUtil.getOrCreateRole(rank), permissions, null));

        action.addPermissionOverride(member, permissions, null);
        action.addPermissionOverride(DiscordConstants.getEveryoneRole(), null, permissions);
        action.addPermissionOverride(DiscordUtil.getOrCreateRole(Rank.TRIAL_MOD), EnumSet.of(Permission.VIEW_CHANNEL), null);

        return action.complete();
    }
}
