package net.exemine.discord.command.impl.staff;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.data.DataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.proxy.ProxyCheck;
import net.exemine.api.proxy.ProxyService;
import net.exemine.api.util.MathUtil;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GeoCompareCommand extends BaseCommand {

    private final DataService dataService;
    private final ProxyService proxyService;

    public GeoCompareCommand(DataService dataService, ProxyService proxyService) {
        super("geocompare", "Compare the real location of two players", true);
        this.dataService = dataService;
        this.proxyService = proxyService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.STRING, "first-name", "Name of the first player", true),
                new OptionData(OptionType.STRING, "second-name", "Name of the second player", true)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.deferReply(false).complete();

        // Getting info for player 1
        String firstName = Objects.requireNonNull(interaction.getOption("first-name")).getAsString();
        CoreData firstData = fetchData(firstName, hook);
        if (firstData == null) return;

        // Getting info for player 2
        String secondName = Objects.requireNonNull(interaction.getOption("second-name")).getAsString();
        CoreData secondData = fetchData(secondName, hook);
        if (secondData == null) return;

        if (firstName.equalsIgnoreCase(secondName)) {
            event.replyEmbeds(EmbedUtil.error("Player names cannot be identical."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        // Both players exist -> start executing checks
        ProxyCheck firstCheck = proxyService.getOrCheckAddress(firstData.getAddress());
        if (hasCheckFailed(firstCheck, firstName, hook)) return;

        ProxyCheck secondCheck = proxyService.getOrCheckAddress(secondData.getAddress());
        if (hasCheckFailed(secondCheck, secondName, hook)) return;

        // Checks passed -> display information
        hook.editOriginalEmbeds(EmbedUtil.create("GEO Check",
                "Found information for both [" + firstCheck.getIsoCode() + "] " + firstData.getName() + " and [" + secondCheck.getIsoCode() + "] " + secondData.getName() + "."
                        + '\n'
                        + "\nCountries: " + DiscordUtil.code(firstCheck.getCountry().equals(secondCheck.getCountry()) ? "Identical" : "Different")
                        + "\nRegions: " + DiscordUtil.code(firstCheck.getRegion().equals(secondCheck.getRegion()) ? "Identical" : "Different")
                        + "\nISPs: " + DiscordUtil.code(firstCheck.getProvider().equals(secondCheck.getProvider()) ? "Identical" : "Different")
                        + "\nDistance: " + DiscordUtil.code(MathUtil.getGeoDistance(firstCheck.getLatitude(), firstCheck.getLongitude(), secondCheck.getLatitude(), secondCheck.getLongitude()) + " KM")
        )).queue();
    }

    private CoreData fetchData(String name, InteractionHook hook) {
        Optional<CoreData> data = dataService.fetch(CoreData.class, name.toLowerCase());

        if (data.isEmpty()) {
            hook.editOriginalEmbeds(EmbedUtil.getUserNeverPlayed(name))
                    .queue();
        }
        return data.orElse(null);
    }

    private boolean hasCheckFailed(ProxyCheck check, String name, InteractionHook hook) {
        boolean failed = check == null;

        if (failed) {
            hook.editOriginalEmbeds(EmbedUtil.error("Failed to execute an IP check for " + DiscordUtil.code(name) + ", contact a developer."))
                    .queue();
        }
        return failed;
    }
}