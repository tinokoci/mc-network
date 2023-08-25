package net.exemine.uhc.team;

import lombok.RequiredArgsConstructor;
import net.exemine.api.util.Executor;
import net.exemine.api.util.MultiValueMap;
import net.exemine.api.util.spigot.ChatColor;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.spigot.Clickable;
import net.exemine.uhc.UHC;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TeamService {

    private final UHC plugin;
    private final GameService gameService;

    private final Map<UUID, Team> teams = new HashMap<>();
    private final MultiValueMap<UUID, TeamInvite> pendingInvites = new MultiValueMap<>();

    private int idCounter = 0;

    private final List<ChatColor> colors = new ArrayList<>() {{
        addAll(List.of(
                ChatColor.DARK_GREEN,
                ChatColor.DARK_RED,
                ChatColor.DARK_PURPLE,
                ChatColor.GOLD,
                ChatColor.DARK_GRAY,
                ChatColor.BLUE,
                ChatColor.AQUA,
                ChatColor.RED,
                ChatColor.LIGHT_PURPLE
        ));
    }};

    public Team createTeam(UHCUser leader) {
        Team team = new Team(plugin, ++idCounter, colors.get(ThreadLocalRandom.current().nextInt(colors.size())), leader);
        teams.put(leader.getUniqueId(), team);
        return team;
    }

    public void addToTeam(UHCUser member, Team team) {
        team.addMember(member);
        teams.put(member.getUniqueId(), team);
        team.sendMessage(CC.PURPLE + "[Team] " + member.getColoredDisplayName() + CC.GRAY + " has joined the team.");
    }

    public void removeFromTeam(UHCUser member, Team team, boolean kick) {
        team.removeMember(member);
        teams.remove(member.getUniqueId());

        if (team.getSize() > 0) {
            team.sendMessage(CC.PURPLE + "[Team] " + member.getColoredDisplayName() + CC.GRAY + " has " + (kick ? "been kicked from" : "left") + " the team.");
        }
    }

    public Team getTeam(UHCUser user) {
        Team team = teams.get(user.getUniqueId());

        // Teams are being created on scatter if someone doesn't have one
        // Afterwards while playing if someone new joins and needs team info just lazy create it
        if (team == null && gameService.isStateOrHigher(GameState.SCATTERING)) {
            return createTeam(user);
        }
        return team;
    }

    public void sendInvite(UHCUser leader, UHCUser target, boolean crossTeam) {
        Team team = leader.getTeam();

        if (hasInvite(target, team)) {
            leader.sendMessage(target.getColoredDisplayName() + CC.RED + " already has a pending " + (crossTeam ? "cross " : "") + "team invite.");
            return;
        }
        TeamInvite invite = new TeamInvite(leader, team);
        pendingInvites.put(target.getUniqueId(), invite);

        if (crossTeam) {
            target.getTeam().getMembers()
                    .stream()
                    .filter(member -> member != target)
                    .forEach(member -> member.sendMessage(CC.PURPLE + "[Team] " + leader.getColoredDisplayName() + CC.GRAY + "'s team want to cross team with yours."));
        }
        new Clickable()
                .add(CC.PURPLE + "[Team] " + leader.getColoredDisplayName() + CC.GRAY + " has sent you a " + (crossTeam ? "cross " : "") + "team invite. ")
                .add(CC.GREEN + "[Accept]", CC.GREEN + "Accept the team invite.", '/' + (crossTeam ? "crossteam" : "team") + " accept " + leader.getDisplayName())
                .add(" ")
                .add(CC.RED + "[Deny]", CC.RED + "Deny the team invite.", '/' + (crossTeam ? "crossteam" : "team") + " deny " + leader.getDisplayName())
                .send(target);
        team.sendMessage(CC.PURPLE + "[Team] " + (crossTeam
                ? target.getColoredDisplayName() + CC.GRAY + "'s team has been invited to cross team with yours."
                : target.getColoredDisplayName() + CC.GRAY + " has been invited to the team."));

        invite.setTaskId(Executor.schedule(() -> {
            if (leader.getTeam().hasMember(target)) return;
            getPendingInvites(target).remove(invite);
            target.sendMessage(CC.RED + "Team invite from " + leader.getColoredDisplayName() + CC.RED + " has expired.");
            if (!invite.hasInviterChangedTeams()) {
                invite.getTeam().sendMessage(target.getColoredDisplayName() + CC.RED + " did not respond to the " + (crossTeam ? "cross " : "") + "team invite.");
            }
        }).runSyncLater(60_000L));
    }

    public void cancelInvite(UHCUser user, TeamInvite invite) {
        Bukkit.getScheduler().cancelTask(invite.getTaskId());
        getPendingInvites(user).remove(invite);
    }

    public boolean hasInvite(UHCUser user, Team team) {
        return getPendingInvites(user)
                .stream()
                .anyMatch(invite -> invite.getTeam() == team);
    }

    public Collection<TeamInvite> getPendingInvites(UHCUser user) {
        return pendingInvites.get(user.getUniqueId());
    }

    // teams.values contains duplicates of Team objects because we're putting it for every player
    // therefore we're converting values to HashSet because it doesn't allow duplicate elements
    public Collection<Team> getTeams() {
        return new HashSet<>(teams.values());
    }

    public List<Team> getAliveTeams() {
        return getTeams()
                .stream()
                .filter(Team::isAlive)
                .collect(Collectors.toList());
    }

    public void clearTeams() {
        teams.clear();
        idCounter = 0;
    }
}
