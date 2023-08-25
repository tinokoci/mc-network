package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.team.TeamInvite;
import net.exemine.uhc.team.TeamService;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CrossTeamCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;
    private final TeamService teamService;

    public CrossTeamCommand(GameService gameService, TeamService teamService) {
        super(List.of("crossteam", "cross", "ct"));
        this.gameService = gameService;
        this.teamService = teamService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (gameService.isSoloGame()) {
            user.sendMessage(CC.RED + "This is not a team game.");
            return;
        }
        if (!gameService.isStateOrHigher(GameState.PLAYING)) {
            user.sendMessage(CC.RED + "You cannot do that in the current game state.");
            return;
        }
        if (!user.isPlaying()) {
            user.sendMessage(CC.RED + "You cannot do that in your current state.");
            return;
        }
        if (args.length == 0) {
            sendUsage(user);
            return;
        }
        Team team = user.getTeam();
        Team targetTeam;
        int totalTeamSize;
        Optional<TeamInvite> teamInvite;

        switch (args[0].toLowerCase()) {
            case "invite":
                if (args.length < 2) {
                    sendUsage(user);
                    return;
                }
                UHCUser target = userService.get(args[1]);
                if (isUserOffline(user, target)) return;

                if (user == target) {
                    user.sendMessage(CC.RED + "You cannot invite yourself to the cross team.");
                    return;
                }
                if (!target.isPlaying()) {
                    user.sendMessage(target.getColoredDisplayName() + CC.RED + " isn't playing this game.");
                    return;
                }
                targetTeam = target.getTeam();
                totalTeamSize = targetTeam.getAllAliveMembers().size() + team.getAllAliveMembers().size();

                if (totalTeamSize > NumberOption.PLAYERS_PER_TEAM.getValue()) {
                    user.sendMessage(CC.RED + "You cannot extend the maximum team size.");
                    return;
                }
                teamService.sendInvite(user, target, true);
                break;
            case "accept":
                if (args.length < 2) {
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
                    targetTeam = invite.getTeam();

                    totalTeamSize = targetTeam.getAllAliveMembers().size() + team.getAllAliveMembers().size();
                    // To be 100% sure
                    if (totalTeamSize > NumberOption.PLAYERS_PER_TEAM.getValue()) {
                        user.sendMessage(CC.RED + "You cannot extend the maximum team size.");
                        return;
                    }
                    if (targetTeam.getCrossTeams().contains(team)) {
                        user.sendMessage(CC.RED + "You are already cross teaming with team " + targetTeam.getId() + ".");
                        return;
                    }
                    team.sendMessage(CC.PURPLE + "[Team] " + CC.GRAY + "You are now cross teaming with team #" + targetTeam.getId() + ".");
                    targetTeam.sendMessage(CC.PURPLE + "[Team] " + CC.GRAY + "You are now cross teaming with team #" + team.getId() + ".");

                    // Link the teams
                    team.getCrossTeams().add(targetTeam);
                    targetTeam.getCrossTeams().add(team);

                    // Merge backpacks
                    if (Scenario.BACKPACKS.isEnabled()) {
                        Supplier<Stream<ItemStack>> teamBackpack = () -> Arrays.stream(team.getBackpack().getContents())
                                .filter(item -> item != null && item.getType() != Material.AIR);
                        Supplier<Stream<ItemStack>> targetTeamBackpack = () -> Arrays.stream(targetTeam.getBackpack().getContents())
                                .filter(item -> item != null && item.getType() != Material.AIR);

                        int backpackSize = 27;
                        int totalBackpackSize = (int) (teamBackpack.get().count() + targetTeamBackpack.get().count());

                        while (backpackSize < totalBackpackSize) {
                            backpackSize += 9;
                        }
                        Inventory backpack = Bukkit.createInventory(null, backpackSize, CC.DARK_GRAY + "Crossteam Backpack");
                        teamBackpack.get().forEach(backpack::addItem);
                        targetTeamBackpack.get().forEach(backpack::addItem);

                        team.setBackpack(backpack);
                        targetTeam.setBackpack(backpack);

                        team.sendMessage(CC.PURPLE + "[Team] " + CC.GRAY + "Your backpacks have been merged.");
                    }
                    // Merge DnD info
                    if (targetTeam.getDoNotDisturbInfo().isActive()) {
                        team.setDoNotDisturbInfo(targetTeam.getDoNotDisturbInfo());
                    } else {
                        targetTeam.setDoNotDisturbInfo(team.getDoNotDisturbInfo());
                    }
                    teamService.cancelInvite(user, invite);
                } else {
                    user.sendMessage(target.getColoredDisplayName() + CC.RED + " didn't send you a cross team invite.");
                }
                break;
            case "deny":
                if (args.length < 2) {
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
                    targetTeam = invite.getTeam();
                    teamService.cancelInvite(user, invite);
                    targetTeam.sendMessage(user.getColoredDisplayName() + CC.RED + " has denied the cross team invite.");
                    user.sendMessage(CC.PURPLE + "[Team] " + CC.GRAY + "You've denied " + target.getColoredDisplayName() + CC.GRAY + "'s cross team invite.");
                } else {
                    user.sendMessage(target.getColoredDisplayName() + CC.RED + " didn't send you a cross team invite.");
                }
                break;
        }
    }

    private void sendUsage(UHCUser user) {
        user.sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------------------");
        user.sendMessage(CC.BOLD_PURPLE + " Cross Team Commands " + CC.GRAY + '-' + CC.WHITE + " Handle cross teaming with these commands");
        user.sendMessage();
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /crossteam invite <player>" + CC.GRAY + " - Invite another team to cross team with your team");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /crossteam accept <player>" + CC.GRAY + " - Accept a cross team invite");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /crossteam deny <player>" + CC.GRAY + " - Deny a cross team invite");
        user.sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------------------");
    }
}
