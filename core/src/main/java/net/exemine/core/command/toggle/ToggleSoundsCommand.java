package net.exemine.core.command.toggle;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleSoundsCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleSoundsCommand() {
        super(List.of("togglesounds", "sounds"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        data.setMessagingSounds(!data.isMessagingSounds());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Settings] " + CC.GRAY + "You will " + StringUtil.formatBooleanCommand(data.isMessagingSounds()) + CC.GRAY + " hear a sound when receiving private messages.");
    }
}
