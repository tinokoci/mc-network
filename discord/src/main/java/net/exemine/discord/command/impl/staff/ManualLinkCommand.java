package net.exemine.discord.command.impl.staff;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.data.DataService;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.link.LinkService;
import net.exemine.api.util.string.DatabaseUtil;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ManualLinkCommand extends BaseCommand {

    private final BulkDataService bulkDataService;
    private final DataService dataService;
    private final LinkService linkService;

    public ManualLinkCommand(BulkDataService bulkDataService, DataService dataService, LinkService linkService) {
        super("manuallink", "Manually link someone's discord account to a minecraft account", true);
        this.bulkDataService = bulkDataService;
        this.dataService = dataService;
        this.linkService = linkService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.USER, "user", "Discord server member", true),
                new OptionData(OptionType.STRING, "name", "Minecraft account name", true)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.deferReply(true).complete();

        User target = Objects.requireNonNull(interaction.getOption("user")).getAsUser();
        String name = Objects.requireNonNull(interaction.getOption("name")).getAsString();

        Optional<CoreData> optionalTargetCoreData = dataService.fetch(CoreData.class, DatabaseUtil.DISCORD_KEY, target.getId());

        if (optionalTargetCoreData.isPresent()) {
            hook.editOriginalEmbeds(EmbedUtil.error("Minecraft account " + DiscordUtil.code(optionalTargetCoreData.get().getName()) + " is already linked to " + target.getAsMention() + '.'))
                    .queue();
            return;
        }
        dataService.fetch(CoreData.class, name).ifPresentOrElse(data -> {
            BulkData bulkData = bulkDataService.getOrCreate(data.getUniqueId());

            if (!MemberUtil.isEqualOrAbove(user, bulkData.getRank())) {
                hook.editOriginalEmbeds(EmbedUtil.error("You cannot manually link that user."))
                        .queue();
                return;
            }
            if (data.isDiscordLinked()) {
                User linkedUser = DiscordUtil.getUser(data.getDiscordUserId());
                String formattedLinkedUser = linkedUser == null
                        ? DiscordUtil.code(data.getName())
                        : linkedUser.getAsMention();
                hook.editOriginalEmbeds(EmbedUtil.error("Minecraft account " + DiscordUtil.code(data.getName()) + " is already linked to " + formattedLinkedUser + '.'))
                        .queue();
                return;
            }
            linkService.linkAccount(data, target.getId());
            hook.editOriginalEmbeds(EmbedUtil.success("You've manually linked " + target.getAsMention() + " to " + DiscordUtil.code(data.getName()) + '.'))
                    .queue();
        }, () -> hook.editOriginalEmbeds(EmbedUtil.error("Minecraft account " + DiscordUtil.code(name) + " has never logged on the network."))
                .queue());
    }
}
