package net.exemine.uhc.command.toggle;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class ToggleSpectatorsCommand extends BaseCommand<UHCUser, UHCData> {

    public ToggleSpectatorsCommand() {
        super(List.of("togglespectators", "togglespecs", "spectators"));
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        data.setShowSpectators(!data.isShowSpectators());
        user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You can " + (data.isShowSpectators() ? CC.GREEN + "now" : CC.RED + "no longer")
                + CC.GRAY + " see other spectators while spectating.");
        user.saveData(true);

        if (user.isSpectating()) {
            userService.getOnlineUsers()
                    .stream()
                    .filter(UHCUser::isRegularSpectator)
                    .forEach(online -> {
                        if (data.isShowSpectators()) {
                            user.showPlayer(online);
                        } else {
                            user.hidePlayer(online);
                        }
                    });
        }
    }
}
