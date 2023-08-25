package net.exemine.core.command.staff;

import net.exemine.api.data.DataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.spigot.UUIDFetcher;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;
import java.util.UUID;

public class UserImportCommand extends BaseCommand<CoreUser, CoreData> {

    private final DataService dataService;

    public UserImportCommand(DataService dataService) {
        super(List.of("userimport"), Rank.ADMIN);
        this.dataService = dataService;
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /userimport <name>");
            return;
        }
        String name = args[0];
        dataService.fetch(CoreData.class, name).ifPresentOrElse(
                target -> user.sendMessage(CC.RED + "That user is already in the database."),
                () -> {
                    UUID uuid = UUIDFetcher.getUUIDOf(name);
                    if (uuid == null) {
                        user.sendMessage(CC.RED + "Cannot find a minecraft account with that name.");
                        return;
                    }
                    CoreData newData = new CoreData();
                    newData.setUniqueId(uuid);
                    newData.updateGeneralData(name, StringUtil.randomID(16));
                    dataService.update(newData);
                    user.sendMessage(CC.PURPLE + "[User] " + CC.GRAY + "You've imported " + CC.GOLD + name + CC.GRAY + " to the database.");
                }
        );
    }
}
