package net.exemine.discord.message;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.exemine.api.log.LogService;
import net.exemine.api.log.LogType;
import net.exemine.api.log.discord.DiscordLog;
import net.exemine.api.util.Executor;
import net.exemine.api.util.TimeUtil;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class MessageLogListener extends ListenerAdapter {

    private final LogService logService;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        Member member = event.getMember();
        if (member == null) return;

        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();

        DiscordLog log = new DiscordLog(
                LogType.DISCORD,
                '[' + TimeUtil.getFullDate(System.currentTimeMillis()) + "] [#" + channel.getName() + "] "
                        + member.getEffectiveName() + ": " + message.getContentRaw(),
                member.getId(),
                channel.getName()
        );
        Executor.schedule(() -> logService.insertLog(log)).runAsync();
    }
}
