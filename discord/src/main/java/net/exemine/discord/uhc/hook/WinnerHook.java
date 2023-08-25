package net.exemine.discord.uhc.hook;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.exemine.api.data.DataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.match.MatchService;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.model.ScenarioName;
import net.exemine.api.util.TimeUtil;
import net.exemine.discord.util.DiscordConstants;

import java.awt.Color;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WinnerHook {

    private final MatchService matchService;
    private final DataService dataService;

    public void run(UHCMatch nextMatch) {
        if (nextMatch == null) return;
        TextChannel channel = DiscordConstants.getChannelWinners();

        sendMessage(channel, nextMatch);
        nextMatch.setSummarySent(true);
        matchService.updateMatch(nextMatch);
    }

    private void sendMessage(TextChannel channel, UHCMatch match) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("UHC Summary");
        embed.setThumbnail("https://i.imgur.com/ILwZKs8.png");
        embed.setFooter("ID: " + match.getId() + " | " + TimeUtil.getDate(System.currentTimeMillis()));
        embed.setColor(Color.decode("#aa00aa"));

        // First Row
        embed.addField("Winner" + (match.getWinnerUuids().size() == 1 ? "" : "s"),
                String.join(", ", match.getRunningInfo().getWinnerNames()), true);
        embed.addField((match.isTeamGame() ? "Team " : "") + "Kills",
                String.valueOf(match.getRunningInfo().getWinningTeamKills()), true);

        // Second Row
        embed.addField("Finished In", TimeUtil.getClockTime(match.getDuration()), true);
        embed.addField("Participants", match.getWinnerUuids().size() + "/" + match.getInitialPlayerCount(), true);
        StringBuilder builder = new StringBuilder();
        if (match.getHostUuid() == null) {
            builder.append("None");
        } else {
            dataService.fetch(CoreData.class, match.getHostUuid()).ifPresentOrElse(
                    host -> builder.append(host.getName()),
                    () -> builder.append("Unknown"));
            if (match.hasSupervisor()) {
                dataService.fetch(CoreData.class, match.getSupervisorUuid()).ifPresent(
                        supervisor -> builder.append(" [Supervised by ").append(supervisor.getName()).append(']')
                );
            }
        }
        embed.addField("Host", builder.toString(), true);

        // Third Row
        embed.addField("Region", "EU", true);
        embed.addField("Mode", match.getMode(), true);
        embed.addField("Nether", (match.isNether() ? "Enabled" : "Disabled"), true);

        // Fourth Row
        embed.addField("Scenarios", match.getScenarios()
                .stream()
                .map(ScenarioName::getName)
                .collect(Collectors.joining(", ")), false);

        // Send Message
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
