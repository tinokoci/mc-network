package net.exemine.uhc.provider;

import lombok.RequiredArgsConstructor;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.provider.NametagProvider;
import net.exemine.core.provider.nametag.NametagInfo;
import net.exemine.core.provider.nametag.NametagService;
import net.exemine.core.util.PacketUtil;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.info.DoNotDisturbInfo;
import net.exemine.uhc.user.info.NoCleanInfo;

@RequiredArgsConstructor
public class UHCNametagProvider implements NametagProvider<UHCUser> {

    private final GameService gameService;

    @Override
    public NametagInfo getNametag(UHCUser toRefresh, UHCUser refreshFor, NametagService<UHCUser> nametagService) {
        String prefix = CC.DARK_GRAY;
        String suffix = "";
        Rank rank = toRefresh.isDisguised() ? Rank.DEFAULT : toRefresh.getRank();

        if (toRefresh.isSpectating()) {
            prefix = toRefresh.getState().getStaffShortPrefix() + CC.GRAY;
        } else if (gameService.isStateOrLower(GameState.LOBBY)) {
            prefix = rank.getColor();
        } else if (gameService.isStateOrHigher(GameState.SCATTERING)) {
            Team toRefreshTeam = toRefresh.getTeam();
            Team toRefreshAssignedTeam = toRefreshTeam.getAssignedTeam();

            prefix = toRefreshTeam.hasMember(refreshFor) ? CC.GREEN
                    : toRefreshTeam.hasCrossTeamMember(refreshFor) ? CC.YELLOW
                    : toRefreshAssignedTeam != null && toRefreshAssignedTeam.hasMember(refreshFor) ? CC.AQUA
                    : CC.RED;
            NoCleanInfo noCleanInfo = toRefresh.getNoCleanInfo();
            DoNotDisturbInfo doNotDisturbInfo = toRefreshTeam.getDoNotDisturbInfo();
            if (noCleanInfo.isActive()) {
                prefix = CC.DARK_RED + "[!] " + CC.RED;
            } else if (doNotDisturbInfo.isActive() && (doNotDisturbInfo.getEnemy().equals(refreshFor.getTeam()) ||
                    doNotDisturbInfo.getEnemy().isCrossTeamingWith(refreshFor.getTeam()))) {
                prefix = CC.DARK_RED + "[" + CC.DARK_RED + CC.BOLD + '⚔' + CC.DARK_RED + "] " + CC.RED;
            }
        }
        // If user is regular spectator, push him at the end on the tablist
        String teamName = (toRefresh.isRegularSpectator() ? PacketUtil.MAX_TEAM_NAME_VALUE : "")
                + PacketUtil.convertNumberToTeamName(rank.getPriority());

        return nametagService.getOrCreate(teamName, prefix, suffix);
    }
}
