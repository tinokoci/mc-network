package net.exemine.discord.command.impl.staff;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.properties.PropertiesService;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class NetworkTipsCommand extends BaseCommand {

    private final PropertiesService propertiesService;

    public NetworkTipsCommand(PropertiesService propertiesService) {
        super("networktips", "Add or remove a network tip", true);
        this.propertiesService = propertiesService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.STRING, "type", "Type of the action", true)
                        .addChoice("ADD", "ADD")
                        .addChoice("REMOVE", "REMOVE"),
                new OptionData(OptionType.STRING, "key", "Key of the tip", true),
                new OptionData(OptionType.STRING, "text", "Text of the tip", false)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        boolean add = Objects.requireNonNull(interaction.getOption("type")).getAsString().equals("ADD");
        String key = Objects.requireNonNull(interaction.getOption("key")).getAsString().toLowerCase();
        OptionMapping textOption = interaction.getOption("text");

        InteractionHook hook = event.deferReply(true).complete();
        Map<String, String> tips = propertiesService.getProperties().getNetworkTips();

        if (add) {
            if (textOption == null) {
                hook.editOriginalEmbeds(EmbedUtil.error("You didn't specify the message")).queue();
                return;
            }
            if (tips.containsKey(key)) {
                hook.editOriginalEmbeds(EmbedUtil.error("A tip with that key already exist.")).queue();
                return;
            }
            String text = textOption.getAsString();
            tips.put(key, text);
            propertiesService.update();
            hook.editOriginalEmbeds(EmbedUtil.success("You've added a tip with the key " + DiscordUtil.code(key) + ".\n"
                            + Arrays.stream(text.split("<nl>"))
                            .map(line -> DiscordUtil.code("-") + ' ' + line)
                            .collect(Collectors.joining("\n"))))
                    .queue();
        } else {
            if (!tips.containsKey(key)) {
                hook.editOriginalEmbeds(EmbedUtil.error("A tip with that key doesn't exist.")).queue();
                return;
            }
            tips.remove(key);
            propertiesService.update();
            hook.editOriginalEmbeds(EmbedUtil.success("You've removed a tip with the key " + DiscordUtil.code(key) + '.')).queue();
        }
    }
}
