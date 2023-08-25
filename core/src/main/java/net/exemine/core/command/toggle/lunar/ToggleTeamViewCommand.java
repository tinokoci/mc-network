package net.exemine.core.command.toggle.lunar;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleTeamViewCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleTeamViewCommand() {
        super(List.of("toggleteamview", "teamview"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        CoreData.LunarData lunarData = data.getLunarData();
        lunarData.setTeamView(!lunarData.isTeamView());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Lunar] " + CC.GRAY + "You will " + StringUtil.formatBooleanCommand(lunarData.isTeamView()) + CC.GRAY + " see the Lunar Client team mate marker.");
    }
}
