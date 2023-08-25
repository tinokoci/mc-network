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
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.log.LogService;
import net.exemine.api.log.minecraft.MinecraftLogType;
import net.exemine.api.util.TimeUtil;
import net.exemine.discord.Discord;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.user.NetworkUser;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class McLogsCommand extends BaseCommand {

    private final BulkDataService bulkDataService;
    private final DataService dataService;
    private final LogService logService;

    public McLogsCommand(Discord discord) {
        super("mclogs", "Lookup minecraft logs for a player", true);
        this.bulkDataService = discord.getBulkDataService();
        this.dataService = discord.getDataService();
        this.logService = discord.getLogService();
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.STRING, "name", "Name of the player", true),
                new OptionData(OptionType.STRING, "type", "Type of the logs", true).addChoices(Arrays.stream(MinecraftLogType.values())
                        .map(type -> new Command.Choice(type.name(), type.name()))
                        .collect(Collectors.toList())),
                new OptionData(OptionType.STRING, "start-date", "Example: " + TimeUtil.DUMMY_DATE, true),
                new OptionData(OptionType.STRING, "end-date", "Example: " + TimeUtil.DUMMY_DATE, true)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.deferReply(true).complete();

        if (!MemberUtil.isLinked(user)) {
            hook.editOriginalEmbeds(EmbedUtil.getNotLinked()).queue();
            return;
        }
        String name = Objects.requireNonNull(interaction.getOption("name")).getAsString();
        MinecraftLogType logType = MinecraftLogType.valueOf(Objects.requireNonNull(interaction.getOption("type")).getAsString());
        String startDate = Objects.requireNonNull(interaction.getOption("start-date")).getAsString();
        String endDate = Objects.requireNonNull(interaction.getOption("end-date")).getAsString();

        if (!TimeUtil.isDate(startDate) || !TimeUtil.isDate(endDate)) {
            hook.editOriginalEmbeds(EmbedUtil.error("Your input contains a wrong time format.")).queue();
            return;
        }
        dataService.fetch(CoreData.class, name).ifPresentOrElse(coreData -> {
            NetworkUser networkUser = NetworkUser.getOrCreate(user, NetworkUser::loadBulkData);
            BulkData bulkData = bulkDataService.getOrCreate(coreData.getUniqueId(), bulkDataService::loadRanks);

            if (!networkUser.isAbove(bulkData)) {
                hook.editOriginalEmbeds(EmbedUtil.error("You cannot fetch logs of this user.")).queue();
                return;
            }
            String url = logService.fetchMinecraftLogs(coreData, logType, TimeUtil.getMillisFromDate(startDate), TimeUtil.getMillisFromDate(endDate));
            hook.editOriginalEmbeds(EmbedUtil.success("Minecraft Logs", "Link: " + url)).queue();
        }, () -> hook.editOriginalEmbeds(EmbedUtil.getUserNeverPlayed(name)).queue());
    }
}
