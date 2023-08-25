package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.team.TeamInvite;
import net.exemine.uhc.team.TeamService;
import net.exemine.uhc.user.UHCUser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TeamCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;
    private final TeamService teamService;

    public TeamCommand(GameService gameService, TeamService teamService) {
        super(List.of("team", "t", "f", "faction"));
        this.gameService = gameService;
        this.teamService = teamService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (gameService.isSoloGame()) {
            user.sendMessage(CC.RED + "This is not a team game.");
            return;
        }
        if (args.length == 0) {
            sendUsage(user);
            return;
        }
        Team team = user.getTeam();

        boolean wrongState = gameService.isStateOrHigher(GameState.SCATTERING);
        boolean canCrossTeam = false; /*gameService.isStateOrHigher(GameState.PLAYING)
                && (System.currentTimeMillis() - gameService.getStartTime() < NumberOption.GRACE_PERIOD.getMinutesInMillis());*/

        Optional<TeamInvite> teamInvite;

        switch (args[0].toLowerCase()) {
            case "create":
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (wrongState) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (team != null) {
                    user.sendMessage(CC.RED + "You're already in a team.");
                    return;
                }
                teamService.createTeam(user);
                user.sendMessage(CC.PURPLE + "[Team] " + CC.GRAY + "You've created a team.");
                break;
            case "leave":
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (wrongState) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (team == null) {
                    user.sendMessage(CC.RED + "You're not in a team.");
                    return;
                }
                boolean disband = team.getSize() == 1;
                teamService.removeFromTeam(user, team, false);
                user.sendMessage(CC.PURPLE + "[Team] " + CC.GRAY + "You've " + (disband ? "disbanded" : "left") + " the team.");

                if (!disband) {
                    team.setLeader(team.getMembers().iterator().next());
                }
                break;
            case "invite":
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (wrongState && !canCrossTeam) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (team == null) {
                    team = teamService.createTeam(user);
                }
                if (team.getLeader() != user) {
                    user.sendMessage(CC.RED + "You're not the team leader.");
                    return;
                }
                if (team.isFull()) {
                    user.sendMessage(CC.RED + "Your team is already at the maximum capacity.");
                    return;
                }
                if (args.length != 2) {
                    sendUsage(user);
                    return;
                }
                UHCUser target = userService.get(args[1]);
                if (isUserOffline(user, target)) return;

                if (user == target) {
                    user.sendMessage(CC.RED + "You cannot invite yourself to the team.");
                    return;
                }
                if (target.isGameModerator()) {
                    user.sendMessage(target.getColoredDisplayName() + CC.RED + " isn't playing this game.");
                    return;
                }
                if (target.inTeamWithTeammates()) {
                    user.sendMessage(target.getColoredDisplayName() + CC.RED + " is already in a team.");
                    return;
                }
                if (team.hasMember(target)) {
                    user.sendMessage(target.getColoredDisplayName() + CC.RED + " is already in your team.");
                    return;
                }
                teamService.sendInvite(user, target, false);
                break;
            case "accept":
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (wrongState && !canCrossTeam) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (user.inTeamWithTeammates()) {
                    user.sendMessage(CC.RED + "You're already in a team.");
                    return;
                }
                if (args.length != 2) {
                    sendUsage(user);
                    return;
                }
                target = userService.retrieve(args[1]);

                if (target == null) {
                    user.sendMessage(Lang.USER_NOT_FOUND);
                    return;
                }
                teamInvite = teamService.getPendingInvites(user)
                        .stream()
                        .filter(invite -> invite.getInviter().getDisplayName().equalsIgnoreCase(args[1]))
                        .findFirst();

                if (teamInvite.isPresent()) {
                    TeamInvite invite = teamInvite.get();
                    Team teamToJoin = invite.getTeam();

                    if (teamToJoin.isFull()) {
                        user.sendMessage(CC.RED + "That team is full.");
                        return;
                    }
                    teamService.cancelInvite(user, invite);

                    if (invite.hasInviterChangedTeams()) {
                        user.sendMessage(CC.RED + "This invite is no longer valid because the inviter has joined another team.");
                        return;
                    }
                    teamService.addToTeam(user, teamToJoin);
                } else {
                    user.sendMessage(target.getColoredDisplayName() + CC.RED + " didn't send you a team invite.");
                }
                break;
            case "deny":
                if (wrongState && !canCrossTeam) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (args.length != 2) {
                    sendUsage(user);
                    return;
                }
                target = userService.retrieve(args[1]);

                if (target == null) {
                    user.sendMessage(Lang.USER_NOT_FOUND);
                    return;
                }
                teamInvite = teamService.getPendingInvites(user)
                        .stream()
                        .filter(invite -> invite.getInviter().getDisplayName().equalsIgnoreCase(args[1]))
                        .findFirst();

                if (teamInvite.isPresent()) {
                    TeamInvite invite = teamInvite.get();
                    Team teamToDeny = invite.getTeam();
                    teamService.cancelInvite(user, invite);
                    teamToDeny.sendMessage(user.getColoredDisplayName() + CC.RED + " has denied the team invite.");
                    user.sendMessage(CC.PURPLE + "[Team] " + CC.GRAY + "You've denied " + target.getColoredDisplayName() + CC.GRAY + "'s team invite.");
                } else {
                    user.sendMessage(target.getColoredDisplayName() + CC.RED + " didn't send you a team invite.");
                }
                break;
            case "promote":
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (team == null) {
                    user.sendMessage(CC.RED + "You're not in a team.");
                    return;
                }
                if (team.getLeader() != user) {
                    user.sendMessage(CC.RED + "You're not the team leader.");
                    return;
                }
                if (args.length != 2) {
                    sendUsage(user);
                    return;
                }
                target = userService.retrieve(args[1]);

                if (target == null) {
                    user.sendMessage(Lang.USER_NOT_FOUND);
                    return;
                }
                if (user == target) {
                    user.sendMessage(CC.RED + "You're already the team leader.");
                    return;
                }
                if (!team.hasMember(target)) {
                    user.sendMessage(target.getColoredDisplayName() + CC.RED + " is not in your team.");
                    return;
                }
                team.setLeader(target);
                break;
            case "kick":
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (wrongState) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (team == null) {
                    user.sendMessage(CC.RED + "You're not in a team.");
                    return;
                }
                if (args.length != 2) {
                    sendUsage(user);
                    return;
                }
                if (team.getLeader() != user) {
                    user.sendMessage(CC.RED + "You're not the team leader.");
                    return;
                }
                target = userService.retrieve(args[1]);

                if (target == null) {
                    user.sendMessage(Lang.USER_NOT_FOUND);
                    return;
                }
                if (user == target) {
                    user.sendMessage(CC.RED + "You cannot kick yourself from the team.");
                    return;
                }
                if (!team.hasMember(target)) {
                    user.sendMessage(target.getColoredDisplayName() + CC.RED + " is not in your team.");
                    return;
                }
                teamService.removeFromTeam(target, team, true);

                if (target.isOnline()) {
                    target.sendMessage(CC.RED + "You've been kicked from the team.");
                }
                break;
            case "list":
                target = args.length == 1
                        ? user
                        : userService.retrieve(args[1]);

                if (target == null) {
                    user.sendMessage(Lang.USER_NOT_FOUND);
                    return;
                }
                team = teamService.getTeam(target);
                if (team == null) {
                    user.sendMessage((user == target ? CC.RED + "You're not" : target.getColoredDisplayName() + CC.RED + " isn't") + " in a team.");
                    return;
                }
                user.sendMessage(CC.STRIKETHROUGH_GRAY + "-----------------------");
                user.sendMessage(team.getColor() + "Team #" + team.getId() + CC.GRAY + " - ("
                                + team.getSize() + '/' + NumberOption.PLAYERS_PER_TEAM.getValue()+ ')' + " [" + team.getTotalKills() + " kills]");
                user.sendMessage(CC.GRAY + "Members: " + team.getMembers().stream()
                        .map(member -> (member.isPlaying() ? CC.GREEN : CC.RED) + member.getDisplayName())
                        .collect(Collectors.joining(CC.GRAY + ", ")));
                if (!team.getCrossTeamMembers().isEmpty()) {
                    user.sendMessage(CC.GRAY + "Cross Team Members: " + team.getCrossTeamMembers().stream()
                            .map(member -> CC.YELLOW + member.getDisplayName())
                            .collect(Collectors.joining(CC.GRAY + ", ")));
                }
                user.sendMessage(CC.STRIKETHROUGH_GRAY + "-----------------------");
                break;
            case "chat":
                if (user.isSpectating()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (team == null) {
                    user.sendMessage(CC.RED + "You're not in a team.");
                    return;
                }
                if (args.length < 2) {
                    sendUsage(user);
                    return;
                }
                String message = StringUtil.join(args, 1);
                team.sendMessage(CC.GRAY + '[' + CC.BOLD_GOLD + "Team" + CC.GRAY + "] " + user.getColoredDisplayName() + CC.GRAY + ": " + CC.YELLOW + message);
                break;
        }
    }

    private void sendUsage(UHCUser user) {
        user.sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------------------");
        user.sendMessage(CC.BOLD_PURPLE + " Team Commands " + CC.GRAY + '-' + CC.WHITE + " Manage the team with these commands");
        user.sendMessage();
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /team create" + CC.GRAY + " - Create a team");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /team leave" + CC.GRAY + " - Leave a team");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /team invite <player>" + CC.GRAY + " - Invite a player to your team");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /team accept <player>" + CC.GRAY + " - Accept a pending team invitation");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /team deny <player>" + CC.GRAY + " - Deny a pending team invitation");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /team promote <player>" + CC.GRAY + " - Promote a team member to the leader");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /team kick <player>" + CC.GRAY + " - Kick a player from your team");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /team list <player>" + CC.GRAY + " - Display someone's team");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /team chat <message>" + CC.GRAY + " - Send a message to your team");
        user.sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------------------");
    }
}
