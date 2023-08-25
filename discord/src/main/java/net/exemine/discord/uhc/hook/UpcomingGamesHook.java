package net.exemine.discord.uhc.hook;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.exemine.api.data.DataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.model.ScenarioName;
import net.exemine.api.properties.Properties;
import net.exemine.api.properties.PropertiesService;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.Lang;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.EmbedUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UpcomingGamesHook {

    private final DataService dataService;
    private final PropertiesService propertiesService;

    public UpcomingGamesHook(PropertiesService propertiesService, DataService dataService) {
        this.propertiesService = propertiesService;
        this.dataService = dataService;
    }

    public void run(List<UHCMatch> matches) {
        Message message = getMessage();
        if (message == null) return;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("UHC Games Calendar");
        embed.setFooter("Connect through: " + Lang.SERVER_IP);
        embed.setColor(EmbedUtil.PURPLE);

        if (matches.isEmpty()) {
            embed.setDescription("There are no scheduled games at the moment.");
        } else {
            UHCMatch nextMatch = matches.get(0);
            embed.addField("Next Match", getMatchFormat(nextMatch, true), false);

            if (matches.size() > 1) {
                StringBuilder builder = new StringBuilder();
                IntStream.range(1, matches.size()).forEach(i -> {
                    String newLine = i != (matches.size() - 1) ? "\n" : "";
                    builder.append(getMatchFormat(matches.get(i), false)).append(newLine);
                });
                embed.addField("Upcoming Matches", builder.toString(), false);
            }
        }
        List<MessageEmbed> previousEmbeds = message.getEmbeds();
        MessageEmbed newContent = embed.build();

        if (!previousEmbeds.isEmpty() && previousEmbeds.get(0).equals(newContent)) return;
        message.editMessageEmbeds(newContent).queue();
    }

    private String getMatchFormat(UHCMatch match, boolean nextMatch) {
        String separator = " | ";
        String hostSection = "";

        if (match.getHostUuid() != null) {
            Optional<CoreData> hostCoreData = dataService.fetch(CoreData.class, match.getHostUuid());
            if (!hostCoreData.isEmpty()) {
                hostSection = hostCoreData.get().getName() + separator;
            }
        }
        String supervisorSection = "";

        if (match.hasSupervisor()) {
            Optional<CoreData> supervisorCoreData = dataService.fetch(CoreData.class, match.getSupervisorUuid());
            if (supervisorCoreData.isPresent()) {
                supervisorSection = "(Supervised by " + supervisorCoreData.get().getName() + ") " + separator;
            }
        }
        String startingSection = nextMatch ? " (Whitelist off in " + (TimeUtil.getNormalDuration(match.getStartTime() - System.currentTimeMillis())) + ')' : "";

        return ":apple:" + separator
                + TimeUtil.getDate(match.getStartTime()) + startingSection + separator
                + match.getMode() + separator
                + hostSection
                + supervisorSection
                + match.getScenarios().stream().map(ScenarioName::getName).collect(Collectors.joining(", ")) + separator
                + "Nether " + (match.isNether() ? "On" : "Off") + separator
                + "[ID: " + match.getId() + ']';
    }

    private Message getMessage() {
        Properties properties = propertiesService.getProperties();
        TextChannel channel = DiscordConstants.getChannelUpcomingMatches();
        return channel.getHistory().retrievePast(100)
                .complete()
                .stream()
                .filter(m -> m.getId().equals(properties.getUpcomingMatchesMessageId()))
                .findFirst()
                .orElseGet(() -> {
                    Message message = channel.sendMessageEmbeds(EmbedUtil.create("UHC Games Calendar", "Waiting for executor heartbeat...")).complete();
                    properties.setUpcomingMatchesMessageId(message.getId());
                    propertiesService.update();
                    return message;
                });
    }
}

