package net.exemine.core.command.staff.toggle;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleStaffMessagesCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleStaffMessagesCommand() {
        super(List.of("togglestaffmessages", "staffmessages"), Rank.TRIAL_MOD);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        CoreData.StaffData staffData = data.getStaffData();

        staffData.setChatMessages(!staffData.isChatMessages());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(staffData.isChatMessages()) + CC.GRAY + " see staff chat messages.");
    }
}
