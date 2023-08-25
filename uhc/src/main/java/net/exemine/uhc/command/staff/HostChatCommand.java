package net.exemine.uhc.command.staff;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserState;

import java.util.List;

public class HostChatCommand extends BaseCommand<UHCUser, UHCData> {

    public HostChatCommand() {
        super(List.of("hostchat", "hc"), Rank.TRIAL_MOD);
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (user.notInState(UHCUserState.MODERATOR, UHCUserState.SUPERVISOR, UHCUserState.HOST)) {
            user.sendMessage(CC.RED + "You cannot speak in " + CC.BOLD + "host" + CC.RED + " chat if you're not moderating the game.");
            return;
        }
        if (args.length != 0) {
            if (!user.getStaffData().isHostChatMessages()) {
                user.sendMessage(CC.RED + "You're trying to speak in the " + CC.BOLD + "host" + CC.RED + " chat, but have disabled these messages in your settings.");
                return;
            }
            user.sendHostMessage(StringUtil.join(args));
            return;
        }
        user.setHostChat(!user.isHostChat());
        user.sendMessage(CC.PURPLE + "[Chat] " + CC.GRAY + "You're " + StringUtil.formatBooleanCommand(user.isHostChat()) + CC.GRAY + " speaking in " + CC.GOLD + "host " + CC.GRAY + "chat.");
    }
}
