package net.exemine.discord.command.impl.staff;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.log.LogService;
import net.exemine.api.log.LogType;
import net.exemine.api.util.TimeUtil;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;

import java.util.List;
import java.util.Objects;

public class DiscordLogsCommand extends BaseCommand {

    private final LogService logService;

    public DiscordLogsCommand(LogService logService) {
        super("discordlogs", "Lookup discord logs for a user", true);
        this.logService = logService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.USER, "user", "Discord server member", true),
                new OptionData(OptionType.STRING, "start-date", "Example: " + TimeUtil.DUMMY_DATE, true),
                new OptionData(OptionType.STRING, "end-date", "Example: " + TimeUtil.DUMMY_DATE, true)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        InteractionHook hook = event.deferReply(true).complete();

        User inputUser = Objects.requireNonNull(interaction.getOption("user")).getAsUser();
        String startDate = Objects.requireNonNull(interaction.getOption("start-date")).getAsString();
        String endDate = Objects.requireNonNull(interaction.getOption("end-date")).getAsString();

        if (!MemberUtil.isAbove(user, inputUser)) {
            hook.editOriginalEmbeds(EmbedUtil.error("You cannot fetch logs of this user.")).queue();
            return;
        }
        if (!TimeUtil.isDate(startDate) || !TimeUtil.isDate(endDate)) {
            hook.editOriginalEmbeds(EmbedUtil.error("Your input contains a wrong time format.")).queue();
            return;
        }
        String url = logService.fetchLogs(LogType.DISCORD, Filters.eq("user-id", inputUser.getId()), TimeUtil.getMillisFromDate(startDate), TimeUtil.getMillisFromDate(endDate));
        hook.editOriginalEmbeds(EmbedUtil.success("Discord Logs", "Link: " + url)).queue();
    }
}
