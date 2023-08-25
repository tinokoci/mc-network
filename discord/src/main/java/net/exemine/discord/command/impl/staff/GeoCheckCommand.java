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

public class GeoCheckCommand extends BaseCommand {

    private final DataService dataService;
    private final ProxyService proxyService;

    public GeoCheckCommand(DataService dataService, ProxyService proxyService) {
        super("geocheck", "Check the real location of a player", true);
        this.dataService = dataService;
        this.proxyService = proxyService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.STRING, "name", "Name of the player", true)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.deferReply(false).complete();
        String name = Objects.requireNonNull(interaction.getOption("name")).getAsString();

        dataService.fetch(CoreData.class, name).ifPresentOrElse(data -> {
            ProxyCheck check = proxyService.getOrCheckAddress(data.getAddress());

            if (check.isUnknown()) {
                hook.editOriginalEmbeds(EmbedUtil.error("Failed to execute an IP check for " + DiscordUtil.code(name) + ", contact a developer."))
                        .queue();
                return;
            }
            hook.editOriginalEmbeds(EmbedUtil.create("GEO Check",
                    "Found information for [" + check.getIsoCode() + "] " + data.getName() + '.'
                            + '\n'
                            + "\nCountry: " + DiscordUtil.code(check.getCountry())
                            + "\nMalicious: " + DiscordUtil.code(check.isMalicious())
            )).queue();
        }, () -> hook.editOriginalEmbeds(EmbedUtil.error("Minecraft account " + DiscordUtil.code(name) + " has never logged on the network."))
                .queue());
    }
}