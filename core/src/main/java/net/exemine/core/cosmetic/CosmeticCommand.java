package net.exemine.core.cosmetic;

import net.exemine.api.data.impl.CoreData;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class CosmeticCommand extends BaseCommand<CoreUser, CoreData> {

    public CosmeticCommand() {
        super(List.of("cosmetic", "cosmetics"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        new CosmeticMenu(user).open();
    }
}
