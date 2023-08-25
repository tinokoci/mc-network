package net.exemine.core.command.toggle;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class TogglePrivateMessagesCommand extends BaseCommand<CoreUser, CoreData> {

    public TogglePrivateMessagesCommand() {
        super(List.of("toggleprivatemessages", "togglepm", "tpm", "msgtoggle", "togglemsg"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        data.setPrivateMessages(!data.isPrivateMessages());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Settings] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(data.isPrivateMessages()) + CC.GRAY + " receive private messages.");
    }
}
