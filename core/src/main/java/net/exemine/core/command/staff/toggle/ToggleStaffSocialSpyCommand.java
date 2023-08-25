package net.exemine.core.command.staff.toggle;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleStaffSocialSpyCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleStaffSocialSpyCommand() {
        super(List.of("togglestaffsocialspy", "socialspy"), Rank.SENIOR_MOD);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        CoreData.StaffData staffData = data.getStaffData();

        staffData.setSocialSpy(!staffData.isSocialSpy());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(staffData.isSocialSpy()) + CC.GRAY + " see private messages between players.");
    }
}
