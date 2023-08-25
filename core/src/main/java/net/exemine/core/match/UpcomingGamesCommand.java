package net.exemine.core.match;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.match.MatchService;
import net.exemine.api.match.MatchState;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UpcomingGamesCommand extends BaseCommand<CoreUser, CoreData> {

    private final MatchService matchService;

    public UpcomingGamesCommand(MatchService matchService) {
        super(List.of("upcominggames", "upcoming", "matches", "uhcs", "games"));
        this.matchService = matchService;
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        List<UHCMatch> matches = matchService.getAllMatches(UHCMatch.class)
                .stream()
                .filter(match -> match.getState() == MatchState.WAITING && !match.isExpired())
                .sorted(Comparator.comparingLong(UHCMatch::getStartTime))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            user.sendMessage("There are no scheduled games at the moment.");
            return;
        }
        new UpcomingGamesMenu(user, matches).open();
    }
}
