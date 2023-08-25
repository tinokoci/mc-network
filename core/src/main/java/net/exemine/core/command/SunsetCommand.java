package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.model.ServerTime;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class SunsetCommand extends BaseCommand<CoreUser, CoreData> {

    public SunsetCommand() {
        super(List.of("sunset"));
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        ServerTime time = ServerTime.SUNSET;

        if (data.getServerTime() == time) {
            user.sendMessage(CC.RED + "Your time is already set to " + CC.BOLD + time.getName() + CC.RED + '.');
            return;
        }
        data.setServerTime(time);
        user.setPlayerTime(time.getValue(), false);
        user.saveData(false);
        user.sendMessage(CC.PURPLE + "[Time] " + CC.GRAY + "You've set your time to " + CC.GOLD + time.getName() + CC.GRAY + '.');
    }
}
