package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Location;

import java.util.List;

public class SendCoordsCommand extends BaseCommand<UHCUser, UHCData> {

    public SendCoordsCommand() {
        super(List.of("sendcoords"));
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        Location location = user.getLocation();
        user.performCommand("team chat " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }
}
