package net.exemine.discord.command.impl.staff;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
import java.util.Optional;

public class ManualUnlinkCommand extends BaseCommand {

    private final BulkDataService bulkDataService;
    private final DataService dataService;
    private final LinkService linkService;

    public ManualUnlinkCommand(BulkDataService bulkDataService, DataService dataService, LinkService linkService) {
        super("manualunlink", "Manually unlink someone's discord account from a minecraft account", true);
        this.bulkDataService = bulkDataService;
        this.dataService = dataService;
        this.linkService = linkService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.USER, "user", "Discord server member", false),
                new OptionData(OptionType.STRING, "name", "Minecraft account name", false)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.deferReply(true).complete();

        OptionMapping targetOption = interaction.getOption("user");
        OptionMapping nameOption = interaction.getOption("name");

        if ((targetOption == null && nameOption == null) || (targetOption != null && nameOption != null)) {
            hook.editOriginalEmbeds(EmbedUtil.error("Please specify one of the arguments.")).queue();
            return;
        }
        Optional<CoreData> optionalCoreData = Optional.empty();

        if (targetOption != null) {
            User target = targetOption.getAsUser();
            optionalCoreData = dataService.fetch(CoreData.class, DatabaseUtil.DISCORD_KEY, target.getId());
        }
        if (nameOption != null) {
            String name = nameOption.getAsString();
            optionalCoreData = dataService.fetch(CoreData.class, name);
        }
        optionalCoreData.ifPresentOrElse(data -> {
            BulkData bulkData = bulkDataService.getOrCreate(data.getUniqueId());

            if (!MemberUtil.isEqualOrAbove(user, bulkData.getRank())) {
                hook.editOriginalEmbeds(EmbedUtil.error("You cannot manually unlink that user."))
                        .queue();
                return;
            }
            String discordUserId = data.getDiscordUserId();
            String formattedLinkedUser = discordUserId == null || DiscordUtil.getUser(discordUserId) == null
                    ? DiscordUtil.code(data.getName())
                    : DiscordUtil.getUser(discordUserId).getAsMention();
            if (!data.isDiscordLinked()) {
                hook.editOriginalEmbeds(EmbedUtil.error("Minecraft account " + DiscordUtil.code(data.getName()) + " is not linked to a discord account."))
                        .queue();
                return;
            }
            linkService.unlinkAccount(data);
            hook.editOriginalEmbeds(EmbedUtil.success("You've manually unlinked " + formattedLinkedUser + " from " + DiscordUtil.code(data.getName()) + '.'))
                    .queue();
        }, () -> {
            String description = nameOption != null
                    ? "Minecraft account " + DiscordUtil.code(nameOption.getAsString()) + " has never logged on the network."
                    : targetOption.getAsUser().getAsMention() + " is not linked to a minecraft account.";
            hook.editOriginalEmbeds(EmbedUtil.error(description)).queue();
        });
    }
}
