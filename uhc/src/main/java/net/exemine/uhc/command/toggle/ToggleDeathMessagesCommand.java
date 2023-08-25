package net.exemine.uhc.command.toggle;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class ToggleDeathMessagesCommand extends BaseCommand<UHCUser, UHCData> {

    public ToggleDeathMessagesCommand() {
        super(List.of("toggledeathmessages"));
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        data.setDeathMessages(!data.isDeathMessages());
        user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You can " + (data.isDeathMessages() ? CC.GREEN + "now" : CC.RED + "no longer")
                + CC.GRAY + " see death messages.");
        user.saveData(true);
    }
}
