package net.exemine.core.cosmetic.rod;

import net.exemine.api.data.impl.CoreData;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class RodTrailsCommand extends BaseCommand<CoreUser, CoreData> {

    public RodTrailsCommand() {
        super(List.of("rodtrail", "rodtrails"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        new RodTrailSelectMenu(user).open();
    }
}
