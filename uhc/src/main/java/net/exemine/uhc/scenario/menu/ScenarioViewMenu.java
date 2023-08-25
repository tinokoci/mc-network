package net.exemine.uhc.scenario.menu;

import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.util.ServerUtil;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.user.UHCUser;

import java.util.concurrent.atomic.AtomicInteger;

public class ScenarioViewMenu extends Menu<UHCUser> {

    public ScenarioViewMenu(UHCUser user) {
        super(user, CC.DARK_GRAY + "Scenario Viewer", ServerUtil.getMenuRowsByElements(Scenario.getEnabledScenarios().size()));
    }

    @Override
    public void update() {
        AtomicInteger index = new AtomicInteger();
        Scenario.getEnabledScenarios().forEach(scenario -> set(index.getAndIncrement(), scenario.getItem(false)));
    }
}
