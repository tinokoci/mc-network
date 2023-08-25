package net.exemine.uhc.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.util.Executor;
import net.exemine.api.util.spigot.ChatColor;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.ServerUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.info.DoNotDisturbInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Setter
@Getter
public class Team {

    private final int id;
    private final String color;
    private final LinkedHashSet<UHCUser> members = new LinkedHashSet<>();
    private final Set<Team> crossTeams = new HashSet<>();
    @Setter
    private Inventory backpack;

    private UHCUser leader;
    private Team assignedTeam;

    private DoNotDisturbInfo doNotDisturbInfo;

    private Location scatterLocation;
    private Location borderShrinkLocation;

    public Team(UHC plugin, int id, ChatColor color, UHCUser leader) {
        this.id = id;
        this.color = color.toString();
        this.leader = leader;
        this.members.add(leader);
        this.backpack = Bukkit.createInventory(null, 27, CC.DARK_GRAY + "Team #" + this.id + "'s Backpack");

        doNotDisturbInfo = new DoNotDisturbInfo(this);

        if (ServerUtil.isServerThread()) {
            scatterLocation = plugin.getLocationService().getGameScatterLocation();
        } else {
            Executor.schedule(() -> scatterLocation = plugin.getLocationService().getGameScatterLocation()).runSync();
        }
    }

    public void sendMessage(String message) {
        Stream.concat(getMembers().stream(), getCrossTeamMembers().stream())
                .map(member -> UHC.get().getUserService().get(member))
                .filter(UHCUser::isOnline)
                .forEach(member -> member.sendMessage(message));
    }

    public void setLeader(UHCUser leader) {
        this.leader = leader;
        sendMessage(CC.PURPLE + "[Team] " + leader.getColoredDisplayName() + CC.GRAY + " has been promoted to the team leader.");
    }

    public void addMember(UHCUser member) {
        members.add(member);
    }

    public void removeMember(UHCUser member) {
        members.remove(member);
    }

    public int getTotalKills() {
        return members
                .stream()
                .mapToInt(user -> user.getGameInfo().getKills().getValue())
                .sum();
    }

    public boolean hasMember(UHCUser user) {
        return members.contains(user);
    }

    public boolean hasCrossTeamMember(UHCUser user) {
        return getCrossTeamMembers().contains(user);
    }

    public boolean isAlive() {
        return !getAliveMembers().isEmpty();
    }

    public List<UHCUser> getAliveMembers() {
        return members
                .stream()
                .filter(UHCUser::isPlaying)
                .collect(Collectors.toList());
    }

    public List<UHCUser> getCrossTeamMembers() {
        return crossTeams.stream()
                .flatMap(team -> team.getAliveMembers().stream()) // Cross team members are only relevant when alive
                .collect(Collectors.toList());
    }

    public List<UHCUser> getAllAliveMembers() {
        return Stream.concat(getAliveMembers().stream(), getCrossTeamMembers().stream())
                .collect(Collectors.toList());
    }

    public boolean isDead() {
        return !isAlive();
    }

    public boolean isFull() {
        return getSize() >= NumberOption.PLAYERS_PER_TEAM.getValue();
    }

    public boolean isCrossTeamingWith(Team other) {
        return other != null && crossTeams.contains(other);
    }

    public int getSize() {
        return members.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
