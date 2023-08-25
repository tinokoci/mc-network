package net.exemine.uhc.command.staff.toggle;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class ToggleSpectatorChatCommand extends BaseCommand<UHCUser, UHCData> {

    public ToggleSpectatorChatCommand() {
        super(List.of("togglespectatorchat"), Rank.TRIAL_MOD);
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        UHCData.StaffData staffData = data.getStaffData();

        staffData.setSpectatorChatMessages(!staffData.isSpectatorChatMessages());
        user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + (staffData.isSpectatorChatMessages() ? CC.GREEN + "now" : CC.RED + "no longer")
                + CC.GRAY + " see spectator chat while moderating.");
        user.saveData(true);
    }
}
