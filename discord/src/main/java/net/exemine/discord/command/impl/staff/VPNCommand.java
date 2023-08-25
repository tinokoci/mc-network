package net.exemine.discord.command.impl.staff;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.data.DataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.proxy.ProxyCheck;
import net.exemine.api.proxy.ProxyCheckState;
import net.exemine.api.proxy.ProxyService;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VPNCommand extends BaseCommand {

    private final DataService dataService;
    private final ProxyService proxyService;

    public VPNCommand(DataService dataService, ProxyService proxyService) {
        super("vpn", "Manage the VPN status for users.", true);
        this.dataService = dataService;
        this.proxyService = proxyService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.STRING, "name", "User to update", true),
                new OptionData(OptionType.STRING, "state", "New VPN state", true)
                        .addChoices(Arrays.stream(ProxyCheckState.values())
                                .map(state -> new Command.Choice(state.name(), state.name()))
                                .collect(Collectors.toList()))
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        String name = Objects.requireNonNull(interaction.getOption("name")).getAsString();
        ProxyCheckState state = ProxyCheckState.get(Objects.requireNonNull(interaction.getOption("state")).getAsString());

        InteractionHook hook = event.deferReply(true).complete();

        dataService.fetch(CoreData.class, name).ifPresentOrElse(
                data -> {
                    ProxyCheck check = proxyService.getOrCheckAddress(data.getAddress());
                    if (state == check.getState()) {
                        hook.editOriginalEmbeds(EmbedUtil.error(DiscordUtil.code(data.getName()) + "'s VPN status is already set to " + DiscordUtil.code(state.getName()) + '.'))
                                .queue();
                        return;
                    }
                    proxyService.updateCheck(check, state);
                    hook.editOriginalEmbeds(EmbedUtil.success("You've updated " + DiscordUtil.code(data.getName()) + "'s VPN status to " + DiscordUtil.code(state.getName()) + '.'))
                            .queue();
                },
                () -> hook.editOriginalEmbeds(EmbedUtil.getUserNeverPlayed(name)).queue());
    }
}
