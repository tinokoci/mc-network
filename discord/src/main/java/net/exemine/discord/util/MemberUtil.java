package net.exemine.discord.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.callable.Callback;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public class MemberUtil {

    public static boolean isEqual(User user, Rank rank) {
        if (!inServer(user)) return false;
        return getRank(getMember(user)).isEqual(rank);
    }

    public static boolean isAbove(User user, Rank rank) {
        if (!inServer(user)) return false;
        return getRank(getMember(user)).isAbove(rank);
    }

    public static boolean isAbove(User user, User target) {
        return isAbove(user, getRank(getMember(target)));
    }

    public static boolean isEqualOrAbove(User user, Rank rank) {
        if (!inServer(user)) return false;
        return getRank(getMember(user)).isEqualOrAbove(rank);
    }

    public static Rank getRank(Member member) {
        return member.getRoles()
                .stream()
                .map(role -> Rank.get(role.getName()))
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(Rank::getPriority))
                .orElse(Rank.DEFAULT);
    }

    public static boolean isLinked(User user) {
        if (!inServer(user)) return false;
        Member member = getMember(user);
        return hasRole(member, DiscordConstants.getRoleLinked()) || hasRole(member, DiscordConstants.getRoleLinkLock());
    }

    public static boolean isLinked(Member member) {
        return isLinked(member.getUser());
    }

    public static boolean hasRole(Member member, Role role) {
        return member.getRoles()
                .stream()
                .anyMatch(r -> r.equals(role));
    }

    public static boolean hasRole(User user, Role role) {
        if (!inServer(user)) return false;
        return hasRole(getMember(user), role);
    }

    public static void modifyMemberRoles(Member member, Collection<Role> toAdd, Collection<Role> toRemove) {
        handleHierarchy(() -> DiscordUtil.getGuild().modifyMemberRoles(member, toAdd, toRemove).queue());
    }

    public static void modifyNickname(Member member, String nickname) {
        handleHierarchy(() -> member.modifyNickname(nickname).queue());
    }

    private static void handleHierarchy(Callback callback) {
        try {
            callback.run();
        } catch (HierarchyException e) {
            System.err.println("Couldn't execute action because of hierarchy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean inServer(User user) {
        return getMember(user.getId()) != null;
    }

    public static Member getMember(String userId) {
        return DiscordUtil.getGuild().getMemberById(userId);
    }

    public static Member getMember(User user) {
        return DiscordUtil.getGuild().getMember(user);
    }
}
