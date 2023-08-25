package net.exemine.core.disguise.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.disguise.DisguiseService;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;

import java.util.List;

public class UndisguiseCommand extends BaseCommand<CoreUser, CoreData> {

    private final DisguiseService disguiseService;

    public UndisguiseCommand(DisguiseService disguiseService) {
        super(List.of("undisguise", "ud", "unnick"));
        this.disguiseService = disguiseService;
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (!disguiseService.canAccess(user)) {
            user.sendMessage(Lang.NO_PERMISSION);
            return;
        }
        if (!user.isDisguised()) {
            user.sendMessage(CC.RED + "You are not disguised.");
            return;
        }
        if (!InstanceUtil.isType(InstanceType.HUB)) {
            user.sendMessage(CC.RED + "You can only undisguise in the hub.");
            return;
        }
        if (user.getVehicle() != null) {
            user.sendMessage(CC.RED + "You cannot undisguise while on a vehicle.");
            return;
        }
        disguiseService.undisguise(user);
        user.sendMessage(CC.PURPLE + "[Disguise] " + CC.GRAY + "You now appear as yourself.");
    }
}
