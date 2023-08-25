package net.exemine.core.command.toggle.lunar;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleTitlesCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleTitlesCommand() {
        super(List.of("toggletitles", "titles"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        CoreData.LunarData lunarData = data.getLunarData();
        lunarData.setTitles(!lunarData.isTitles());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Lunar] " + CC.GRAY + "You will " + StringUtil.formatBooleanCommand(lunarData.isTitles()) + CC.GRAY + " see Lunar Client titles.");
    }
}
