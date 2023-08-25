package net.exemine.core.command.toggle.lunar;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleBorderCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleBorderCommand() {
        super(List.of("toggleborder", "border"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        CoreData.LunarData lunarData = data.getLunarData();
        lunarData.setBorder(!lunarData.isBorder());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Lunar] " + CC.GRAY + "You will " + StringUtil.formatBooleanCommand(lunarData.isBorder()) + CC.GRAY + " see the Lunar Client border.");
    }
}
