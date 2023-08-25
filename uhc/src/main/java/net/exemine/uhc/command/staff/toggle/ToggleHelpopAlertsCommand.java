package net.exemine.uhc.command.staff.toggle;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class ToggleHelpopAlertsCommand extends BaseCommand<UHCUser, UHCData> {

    public ToggleHelpopAlertsCommand() {
        super(List.of("togglehelpopalerts"), Rank.TRIAL_MOD);
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        UHCData.StaffData staffData = data.getStaffData();

        staffData.setHelpOpAlerts(!staffData.isHelpOpAlerts());
        user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + (staffData.isHelpOpAlerts() ? CC.GREEN + "now" : CC.RED + "no longer")
                + CC.GRAY + " see helpop alerts while moderating.");
        user.saveData(true);
    }
}
