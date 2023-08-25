package net.exemine.core.cosmetic.color;

import net.exemine.api.data.impl.CoreData;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ColorCommand extends BaseCommand<CoreUser, CoreData> {

    public ColorCommand() {
        super(List.of("color", "colors", "namecolor", "namecolors"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        new ColorSelectMenu(user).open();
    }
}
