package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.UHC;
import net.exemine.uhc.config.menu.ConfigViewMenu;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class ConfigCommand extends BaseCommand<UHCUser, UHCData> {

    private final UHC plugin;

    public ConfigCommand(UHC plugin) {
        super(List.of("config", "configuration", "list"));
        this.plugin = plugin;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        new ConfigViewMenu(user, plugin).open();
    }
}
