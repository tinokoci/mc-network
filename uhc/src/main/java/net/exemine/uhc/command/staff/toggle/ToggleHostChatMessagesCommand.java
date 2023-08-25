package net.exemine.uhc.command.staff.toggle;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class ToggleHostChatMessagesCommand extends BaseCommand<UHCUser, UHCData> {

    public ToggleHostChatMessagesCommand() {
        super(List.of("togglehostchatmessages"), Rank.TRIAL_MOD);
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        UHCData.StaffData staffData = data.getStaffData();

        staffData.setHostChatMessages(!staffData.isHostChatMessages());
        user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + (staffData.isHostChatMessages() ? CC.GREEN + "now" : CC.RED + "no longer")
                + CC.GRAY + " see host chat messages while moderating.");
        user.saveData(true);
    }
}
