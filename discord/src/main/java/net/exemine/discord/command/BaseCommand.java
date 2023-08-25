package net.exemine.discord.command;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.util.Executor;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
public abstract class BaseCommand extends ListenerAdapter {

    private final String name;
    private final String description;
    private final ChannelType channelType;
    private final boolean staffOnly;

    @Setter
    private boolean async;

    @Setter
    private List<OptionData> optionData = List.of();

    public BaseCommand(String name, String description, boolean staffOnly) {
        this(name, description, ChannelType.TEXT, staffOnly);
    }

    public BaseCommand(String name, String description, ChannelType channelType) {
        this(name, description, channelType, false);
    }

    public BaseCommand(String name, String description) {
        this(name, description, ChannelType.TEXT, false);
    }

    public BaseCommand setup(JDA jda) {
        DefaultMemberPermissions permissions = staffOnly
                ? DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                : DefaultMemberPermissions.ENABLED;

        jda.upsertCommand(name, description)
                .addOptions(optionData)
                .setDefaultPermissions(permissions)
                .queue();
        return this;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        SlashCommandInteraction interaction = event.getInteraction();
        if (!event.getName().equals(name)) return;

        if (channelType != null && interaction.getChannelType() != channelType) {
            event.replyEmbeds(EmbedUtil.error("This command can only be used in the " + DiscordUtil.code(channelType.name()) + " channel type."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        Executor.schedule(() -> execute(event.getUser(), event.getGuild(), interaction, event)).run(async);
    }

    protected abstract void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event);
}
