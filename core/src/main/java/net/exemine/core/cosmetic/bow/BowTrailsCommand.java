package net.exemine.core.cosmetic.bow;

import net.exemine.api.data.impl.CoreData;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class BowTrailsCommand extends BaseCommand<CoreUser, CoreData> {

    public BowTrailsCommand() {
        super(List.of("bowtrail", "bowtrails"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        new BowTrailSelectMenu(user).open();
    }
}
