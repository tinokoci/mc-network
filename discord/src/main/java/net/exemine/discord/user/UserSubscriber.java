package net.exemine.discord.user;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.exemine.api.rank.Rank;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.redis.pubsub.model.DiscordUserUpdateModel;
import net.exemine.api.redis.pubsub.model.generic.StringModel;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.MemberUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserSubscriber {

    private final RedisService redisService;

    public UserSubscriber(RedisService redisService) {
        this.redisService = redisService;

        subscribeToUserUpdates();
        subscribeToUnlinkRequests();
    }

    private void subscribeToUserUpdates() {
        redisService.subscribe(RedisMessage.DISCORD_USER_UPDATE, DiscordUserUpdateModel.class, model -> {
            Member member = MemberUtil.getMember(model.getUserId());
            if (member == null) return;

            List<Role> rolesToAdd = new ArrayList<>();
            List<Role> rolesToRemove = new ArrayList<>();

            // Add only "Link Lock" and remove "Linked" if user is locked
            if (model.isLocked()) {
                if (MemberUtil.hasRole(member, DiscordConstants.getRoleLinked())) {
                    rolesToRemove.add(DiscordConstants.getRoleLinked());
                }
                if (!MemberUtil.hasRole(member, DiscordConstants.getRoleLinkLock())) {
                    rolesToAdd.add(DiscordConstants.getRoleLinkLock());
                }
                MemberUtil.modifyMemberRoles(member, rolesToAdd, rolesToRemove);
                return;
            }
            List<Rank> ranks = model.getRanks();

            // Add "Linked" role
            if (!MemberUtil.hasRole(member, DiscordConstants.getRoleLinked())) {
                rolesToAdd.add(DiscordConstants.getRoleLinked());
            }
            // Remove "Link Lock" role if user has it
            if (MemberUtil.hasRole(member, DiscordConstants.getRoleLinkLock())) {
                rolesToRemove.add(DiscordConstants.getRoleLinkLock());
            }
            // Add "Member" role if user doesn't have other ranks
            if (ranks.isEmpty() && !MemberUtil.hasRole(member, DiscordConstants.getRoleMember())) {
                rolesToAdd.add(DiscordConstants.getRoleMember());
            }
            // Remove "Member" role if user has other ranks
            if (!ranks.isEmpty() && MemberUtil.hasRole(member, DiscordConstants.getRoleMember())) {
                rolesToRemove.add(DiscordConstants.getRoleMember());
            }

            // Add all user's ranks from the server
            ranks.stream()
                    .map(DiscordUtil::getOrCreateRole)
                    .filter(role -> !MemberUtil.hasRole(member, role))
                    .forEach(rolesToAdd::add);

            // Remove all ranks that user doesn't have on the server
            member.getRoles()
                    .stream()
                    .filter(role -> Rank.get(role.getName()) != null)
                    .forEach(role -> {
                        Rank rank = Rank.get(role.getName());
                        if (!ranks.contains(rank)) {
                            rolesToRemove.add(role);
                        }
                    });
            MemberUtil.modifyNickname(member, model.getNickname());
            MemberUtil.modifyMemberRoles(member, rolesToAdd, rolesToRemove);
        });
    }

    private void subscribeToUnlinkRequests() {
        redisService.subscribe(RedisMessage.DISCORD_UNLINK_REQUEST, StringModel.class, model -> {
            Member member = MemberUtil.getMember(model.getMessage());
            if (member == null) return;
            boolean linkLock = MemberUtil.hasRole(member, DiscordConstants.getRoleLinkLock());

            List<Role> rolesToAdd = new ArrayList<>();
            List<Role> rolesToRemove = new ArrayList<>();

            if (MemberUtil.hasRole(member, DiscordConstants.getRoleLinked())) {
                rolesToRemove.add(DiscordConstants.getRoleLinked());
            }
            // If link locked -> remove that role, but keep everything else
            if (linkLock) {
                rolesToRemove.add(DiscordConstants.getRoleLinkLock());
            } else {
                Arrays.stream(Rank.values())
                        .map(DiscordUtil::getOrCreateRole)
                        .filter(role -> member.getRoles().contains(role))
                        .forEach(rolesToRemove::add);
                if (!MemberUtil.hasRole(member, DiscordConstants.getRoleMember())) {
                    rolesToAdd.add(DiscordConstants.getRoleMember());
                }
                MemberUtil.modifyNickname(member, member.getUser().getName());
            }
            MemberUtil.modifyMemberRoles(member, rolesToAdd, rolesToRemove);
        });
    }
}
