package net.exemine.discord.claim;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.match.MatchService;
import net.exemine.api.match.MatchState;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.DatabaseUtil;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.user.NetworkUser;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;

import java.util.List;
import java.util.Objects;

public class UnclaimCommand extends BaseCommand {

    private final MatchService matchService;

    public UnclaimCommand(MatchService matchService) {
        super("unclaim", "Unclaims a UHC match.", true);
        this.matchService = matchService;
        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.STRING, "match-id", "ID of the UHC match", true)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        String matchId = Objects.requireNonNull(interaction.getOption("match-id")).getAsString();
        InteractionHook hook = event.deferReply(true).complete();

        matchService.fetchMatch(UHCMatch.class, Filters.eq(DatabaseUtil.PRIMARY_KEY, matchId)).ifPresentOrElse(match -> {
            if (match.getState() != MatchState.WAITING) {
                hook.editOriginalEmbeds(EmbedUtil.error("That match is not in the waiting state.")).queue();
                return;
            }
            if (!MemberUtil.isEqualOrAbove(user, Rank.MOD_PLUS)
                    && !NetworkUser.getOrCreate(user, NetworkUser::loadBulkData).getCoreData().getUniqueId().equals(match.getHostUuid())) {
                hook.editOriginalEmbeds(EmbedUtil.error("You cannot unclaim that match.")).queue();
                return;
            }
            match.setState(MatchState.INVALID);
            match.setNote("Unclaimed (By: " + user.getId() + ')');
            matchService.updateMatch(match);
            hook.editOriginalEmbeds(EmbedUtil.success("You've unclaimed the match with the id " + DiscordUtil.code(matchId) + '.')).queue();
        }, () -> hook.editOriginalEmbeds(EmbedUtil.error("Cannot find an UHC match with that id.")).queue());
    }
}
