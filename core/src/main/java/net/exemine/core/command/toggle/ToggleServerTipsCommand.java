package net.exemine.core.command.toggle;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleServerTipsCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleServerTipsCommand() {
        super(List.of("toggleservertips", "servertips", "tips"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        data.setServerTips(!data.isServerTips());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Settings] " + CC.GRAY + "You will " + StringUtil.formatBooleanCommand(data.isServerTips()) + CC.GRAY + " receive helpful tips about the server.");
    }
}