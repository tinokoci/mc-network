package net.exemine.core.command.staff.toggle;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleInstanceAlertsCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleInstanceAlertsCommand() {
        super(List.of("toggleinstancealerts", "instancealerts"), Rank.DEVELOPER);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        CoreData.StaffData staffData = data.getStaffData();
        staffData.setInstanceAlerts(!staffData.isInstanceAlerts());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Dev] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(staffData.isInstanceAlerts()) + CC.GRAY + " see instance alerts.");
    }
}
