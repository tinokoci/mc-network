package net.exemine.core.match;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.match.MatchService;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MatchHistoryCommand extends BaseCommand<CoreUser, CoreData> {

    private final MatchService matchService;

    public MatchHistoryCommand(MatchService matchService) {
        super(List.of("matchhistory"));
        this.matchService = matchService;
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /matchhistory <player>");
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(target -> {
            List<UHCMatch> matches = matchService.getAllMatches(UHCMatch.class)
                    .stream()
                    .filter(match -> match.isCompleted() && match.getParticipants().contains(target.getUniqueId()))
                    .sorted(Comparator.comparing(UHCMatch::getStartTime, Comparator.reverseOrder()))
                    .collect(Collectors.toList());

            if (matches.isEmpty()) {
                user.sendMessage((user == target ? CC.RED + "You have" : target.getColoredRealName())
                        + CC.RED + " never played an UHC match.");
                return;
            }
            new MatchHistoryMenu(user, target, matches).open();
        }, () -> user.sendMessage(Lang.USER_NEVER_PLAYED));
    }
}
