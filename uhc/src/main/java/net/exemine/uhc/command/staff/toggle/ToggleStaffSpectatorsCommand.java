package net.exemine.uhc.command.staff.toggle;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class ToggleStaffSpectatorsCommand extends BaseCommand<UHCUser, UHCData> {

    public ToggleStaffSpectatorsCommand() {
        super(List.of("togglestaffspectators"), Rank.TRIAL_MOD);
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        UHCData.StaffData staffData = data.getStaffData();

        staffData.setShowGameModerators(!staffData.isShowGameModerators());
        user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + (staffData.isShowGameModerators() ? CC.GREEN + "now" : CC.RED + "no longer")
                + CC.GRAY + " see other game moderators while spectating.");
        user.saveData(true);

        if (user.isGameModerator()) {
            userService.getOnlineUsers()
                    .stream()
                    .filter(UHCUser::isGameModerator)
                    .forEach(online -> {
                        if (staffData.isShowGameModerators()) {
                            user.showPlayer(online);
                        } else {
                            user.hidePlayer(online);
                        }
                    });
        }
    }
}
