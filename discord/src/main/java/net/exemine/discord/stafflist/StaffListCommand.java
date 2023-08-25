package net.exemine.discord.stafflist;

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
import net.exemine.api.util.string.DatabaseUtil;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class StaffListCommand extends BaseCommand {

    private final DataService dataService;
    private final StaffListService staffListService;

    public StaffListCommand(DataService dataService, StaffListService staffListService) {
        super("stafflist", "Update staff list management positions", true);
        this.dataService = dataService;
        this.staffListService = staffListService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.USER, "user", "User to update", true),
                new OptionData(OptionType.STRING, "position", "Position to set ('none' to remove)", true)
                        .addChoices(Arrays.stream(StaffListPosition.values()).map(position -> new Command.Choice(position.getName(), position.getName())).collect(Collectors.toList()))
                        .addChoice("none", "None")
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        User target = Objects.requireNonNull(interaction.getOption("user")).getAsUser();
        String position = Objects.requireNonNull(interaction.getOption("position")).getAsString();

        InteractionHook hook = event.deferReply(true).complete();

        dataService.fetch(CoreData.class, DatabaseUtil.DISCORD_KEY, target.getId()).ifPresentOrElse(data -> {
            UUID uuid = data.getUniqueId();
            boolean clear = position.equalsIgnoreCase("none");
            if (clear && !staffListService.hasPosition(uuid)) {
                hook.editOriginalEmbeds(EmbedUtil.error(target.getAsMention() + " doesn't have a position."))
                        .queue();
                return;
            }
            if (staffListService.hasPosition(uuid) && staffListService.getPosition(uuid).getName().equals(position)) {
                hook.editOriginalEmbeds(EmbedUtil.error(target.getAsMention() + " already has the position " + DiscordUtil.code(position) + '.'))
                        .queue();
                return;
            }
            if (clear) {
                staffListService.updatePosition(uuid, null);
                hook.editOriginalEmbeds(EmbedUtil.success("You've cleared " + target.getAsMention() + "'s position."))
                        .queue();
                return;
            }
            staffListService.updatePosition(uuid, position);
            hook.editOriginalEmbeds(EmbedUtil.success("You've updated " + target.getAsMention() + "'s position to " + DiscordUtil.code(position) + '.'))
                    .queue();
        }, () -> hook.editOriginalEmbeds(EmbedUtil.error(target.getAsMention() + " is not linked to a minecraft account.")
        ).queue());
    }
}
