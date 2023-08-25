package net.exemine.core.command.staff.toggle;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleStaffServerSwitchCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleStaffServerSwitchCommand() {
        super(List.of("togglestaffserverswitch", "serverswitch"), Rank.TRIAL_MOD);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        CoreData.StaffData staffData = data.getStaffData();

        staffData.setServerSwitch(!staffData.isServerSwitch());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(staffData.isServerSwitch()) + CC.GRAY + " see staff server switch messages.");
    }
}
