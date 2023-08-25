package net.exemine.discord.uhc.hook;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.exemine.api.data.DataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.match.MatchService;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.model.ScenarioName;
import net.exemine.api.twitter.TwitterService;
import net.exemine.api.twitter.TwitterType;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.Lang;
import net.exemine.api.util.string.LineAppender;
import net.exemine.api.util.string.Symbol;
import net.exemine.discord.util.DiscordConstants;

import java.awt.Color;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MatchAlertsHook {

    private final MatchService matchService;
    private final DataService dataService;
    private final TwitterService twitterService;

    public void run(UHCMatch nextMatch) {
        if (nextMatch == null) return;
        GuildMessageChannel channel = DiscordConstants.getChannelMatchAlerts();
        int minutesBeforeStart = (int) ((nextMatch.getStartTime() - System.currentTimeMillis()) / 1000 / 60);
        if (!nextMatch.isSecondAlert() && minutesBeforeStart <= 15) {
            nextMatch.setSecondAlert(true);
            sendMessage(channel, nextMatch);
        } else if (!nextMatch.isFirstAlert() && minutesBeforeStart <= 30) {
            nextMatch.setFirstAlert(true);
            sendMessage(channel, nextMatch);
        } else return; // this prevents code below to run if match wasn't altered

        matchService.updateMatch(nextMatch);
    }

    private void sendMessage(GuildMessageChannel channel, UHCMatch match) {
        sendDiscordEmbed(channel, match);
        sendTwitterStatus(match);
    }

    private void sendDiscordEmbed(GuildMessageChannel channel, UHCMatch match) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("New Upcoming UHC!");
        embed.setThumbnail("https://i.imgur.com/ILwZKs8.png");
        embed.setFooter("Connect through: " + Lang.SERVER_IP);
        embed.setColor(Color.decode("#aa00aa"));

        // First Row
        embed.addField("Region", "EU", true);
        embed.addField("Mode", match.getMode(), true);
        embed.addField("Nether", (match.isNether() ? "Enabled" : "Disabled"), true);

        // Second Row
        embed.addField("Start time", TimeUtil.getDate(match.getStartTime()), true);
        embed.addField("Whitelist off in", TimeUtil.getFullDuration(match.getStartTime() - System.currentTimeMillis()), true);

        StringBuilder builder = new StringBuilder();
        dataService.fetch(CoreData.class, match.getHostUuid()).ifPresentOrElse(
                host -> builder.append(host.getName()),
                () -> builder.append("Unknown"));
        if (match.hasSupervisor()) {
            dataService.fetch(CoreData.class, match.getSupervisorUuid()).ifPresent(
                    supervisor -> builder.append(" [Supervised by ").append(supervisor.getName()).append(']')
            );
        }
        embed.addField("Host", builder.toString(), true);

        // Third Row
        embed.addField("Scenarios", match.getScenarios()
                .stream()
                .map(ScenarioName::getName)
                .collect(Collectors.joining(", ")), false);

        // Send Message
        channel.sendMessage(DiscordConstants.getRoleUHCAlerts().getAsMention())
                .setEmbeds(embed.build())
                .queue();
    }

    private void sendTwitterStatus(UHCMatch match) {
        twitterService.tweet(TwitterType.FEED, new LineAppender()
                .append(Symbol.CROSSED_SWORDS + " Upcoming Exemine UHC " + Symbol.CROSSED_SWORDS)
                .append("")
                .append(Symbol.MOUSE + ' ' + match.getMode() + " | " + match.getScenarios()
                        .stream()
                        .map(ScenarioName::getName)
                        .collect(Collectors.joining(", ")))
                .append(Symbol.JOYSTICK + " Nether: " + StringUtil.formatBooleanLong(match.isNether()))
                .append(Symbol.GLOBE_EU_AF + " Region: Europe")
                .append(Symbol.CLOCK + " Opens: " + TimeUtil.getTime(match.getStartTime()) + " (in " + (TimeUtil.getNormalDuration(match.getStartTime() - System.currentTimeMillis())) + ')')
                .append("")
                .append(Symbol.TRIANGULAR_FLAG + " IP: " + Lang.SERVER_IP + " | /join UHC")
                .toString()
        );
    }
}
