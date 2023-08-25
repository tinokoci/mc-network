package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.leaderboard.LeaderboardService;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.leaderboard.LeaderboardsMenu;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;

import java.util.List;

public class LeaderboardsCommand extends BaseCommand<UHCUser, UHCData> {

    private final UHCUserService userService;
    private final LeaderboardService<UHCData> leaderboardService;

    public LeaderboardsCommand(UHCUserService userService, LeaderboardService<UHCData> leaderboardService) {
        super(List.of("leaderboards", "leaderboard", "lb", "lbs"));
        this.userService = userService;
        this.leaderboardService = leaderboardService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        new LeaderboardsMenu(user, leaderboardService, userService).open();
    }
}
