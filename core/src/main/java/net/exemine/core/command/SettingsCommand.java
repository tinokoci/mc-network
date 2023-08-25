package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.settings.SettingsMenu;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class SettingsCommand extends BaseCommand<CoreUser, CoreData> {

    public SettingsCommand() {
        super(List.of("settings", "setting", "options", "option"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        new SettingsMenu(user).open();
    }
}
