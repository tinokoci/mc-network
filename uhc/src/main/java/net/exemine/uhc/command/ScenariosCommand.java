package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.scenario.menu.ScenarioViewMenu;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class ScenariosCommand extends BaseCommand<UHCUser, UHCData> {

    public ScenariosCommand() {
        super(List.of("scenarios", "scenario", "explain"));
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (Scenario.getEnabledScenarios().isEmpty()) {
            user.sendMessage(CC.RED + "There are no active scenarios at the moment.");
            return;
        }
        new ScenarioViewMenu(user).open();
    }
}
