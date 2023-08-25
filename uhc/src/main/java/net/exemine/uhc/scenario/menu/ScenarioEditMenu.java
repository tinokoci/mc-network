package net.exemine.uhc.scenario.menu;

import net.exemine.api.util.string.CC;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.user.UHCUser;

import java.util.concurrent.atomic.AtomicInteger;

public class ScenarioEditMenu extends PaginatedMenu<UHCUser> {

    public ScenarioEditMenu(UHCUser user) {
        super(user, CC.DARK_GRAY + "Scenario Editor", 5, 3);
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addExitItem();
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        Scenario.getAllScenarios().forEach(scenario -> paginate(index.getAndIncrement(), scenario.getItem(true))
                .onClick(scenario::toggle)
        );
    }
}
