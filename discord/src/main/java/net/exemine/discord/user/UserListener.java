package net.exemine.discord.user;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.exemine.api.link.LinkService;
import net.exemine.api.util.Executor;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class UserListener extends ListenerAdapter {

    private final LinkService linkService;

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        User user = event.getUser();
        if (user.isBot()) return;
        Member member = event.getMember();

        Executor.schedule(() -> {
            boolean successfullyLinked = linkService.sendDiscordUpdate(user.getId());
            if (successfullyLinked) return;
            MemberUtil.modifyMemberRoles(member, List.of(DiscordConstants.getRoleMember(), DiscordConstants.getRoleUHCAlerts()), null);
        }).runAsync();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MessageChannelUnion channel = event.getChannel();
        Message message = event.getMessage();

        if (channel == DiscordConstants.getChannelBotCommands()) {
            message.delete().queueAfter(30, TimeUnit.SECONDS);
        }
        /*
        User user = event.getAuthor();
        if (user.isBot()) return; // allow deletion of bot messages in bot commands as well

        if (channel == DiscordConstants.getChannelStaffSuggestions()) {
            String suggestion = event.getMessage().getContentRaw();
            message.delete().queue();

            Member member = event.getMember();
            if (member == null) return;

            String sender = member.getUser().getAsTag() + (member.getNickname() != null ? " - " + member.getNickname() : "");
            message = event.getChannel().sendMessageEmbeds(EmbedUtil.create("New Staff Suggestion", suggestion, "Suggested by " + sender)).complete();

            Emoji yes = DiscordConstants.getEmojiYes();
            Emoji no = DiscordConstants.getEmojiNo();

            if (yes != null) {
                message.addReaction(yes).queue();
            }
            if (no != null) {
                message.addReaction(no).queue();
            }
        }*/
    }
}
