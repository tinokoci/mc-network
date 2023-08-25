package net.exemine.discord.command.impl.staff;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.model.Motd;
import net.exemine.api.properties.PropertiesService;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.List;

public class MotdCommand extends BaseCommand {

    private final PropertiesService propertiesService;

    public MotdCommand(PropertiesService propertiesService) {
        super("motd", "Get or update the network MOTD", true);
        this.propertiesService = propertiesService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.STRING, "line", "Line of the MOTD", false)
                        .addChoice("1", "1")
                        .addChoice("2", "2"),
                new OptionData(OptionType.STRING, "text", "New text of the line", false)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        OptionMapping lineOption = interaction.getOption("line");
        OptionMapping textOption = interaction.getOption("text");

        InteractionHook hook = event.deferReply(true).complete();
        Motd motd = propertiesService.getProperties().getMotd();

        if (lineOption != null && textOption != null) {
            String line = lineOption.getAsString();
            String text = textOption.getAsString();
            switch (line) {
                case "1":
                    if (motd.getLine1().equals(text)) {
                        hook.editOriginalEmbeds(EmbedUtil.error("Line " + DiscordUtil.code('1') + "of the motd is already set to: " + DiscordUtil.bold(text)))
                                .queue();
                        return;
                    }
                    motd.setLine1(text);
                    propertiesService.update();
                    hook.editOriginalEmbeds(EmbedUtil.success("You've updated line " + DiscordUtil.code('1') + "of the motd to: " + DiscordUtil.bold(text)))
                            .queue();
                    break;
                case "2":
                    if (motd.getLine2().equals(text)) {
                        hook.editOriginalEmbeds(EmbedUtil.error("Line " + DiscordUtil.code('2') + "of the motd is already set to: " + DiscordUtil.bold(text)))
                                .queue();
                        return;
                    }
                    motd.setLine2(text);
                    propertiesService.update();
                    hook.editOriginalEmbeds(EmbedUtil.create("Motd Update", "You've updated line " + DiscordUtil.code('2') + "of the motd to: " + DiscordUtil.bold(text)))
                            .queue();
            }
            return;
        }
        hook.editOriginalEmbeds(EmbedUtil.create("Motd Viewer", motd.getCombined()))
                .queue();
    }
}
