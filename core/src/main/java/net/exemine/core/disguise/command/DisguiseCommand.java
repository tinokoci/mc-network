package net.exemine.core.disguise.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.Rank;
import net.exemine.api.texture.TextureService;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.disguise.DisguiseService;
import net.exemine.core.disguise.action.DisguiseAction;
import net.exemine.core.disguise.entry.DisguiseEntryType;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;

import java.util.List;

public class DisguiseCommand extends BaseCommand<CoreUser, CoreData> {

    private final DisguiseService disguiseService;
    private final TextureService textureService;

    public DisguiseCommand(DisguiseService disguiseService, TextureService textureService) {
        super(List.of("disguise", "d", "nick"));
        this.disguiseService = disguiseService;
        this.textureService = textureService;
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (!disguiseService.canAccess(user)) {
            user.sendMessage(Lang.NO_PERMISSION);
            return;
        }
        if (true) {
            user.sendMessage(CC.RED + "This feature is disabled in beta.");
            return;
        }
        if (user.isDisguised()) {
            user.sendMessage(CC.RED + "You are already disguised.");
            return;
        }
        if (!InstanceUtil.isType(InstanceType.HUB)) {
            user.sendMessage(CC.RED + "You can only disguise in the hub.");
            return;
        }
        if (user.getVehicle() != null) {
            user.sendMessage(CC.RED + "You cannot disguise while on a vehicle.");
            return;
        }
        if (args.length > 2) {
            user.sendMessage(CC.RED + "Usage: /disguise [name] [skin]");
            return;
        }
        String name = args.length > 0
                ? args[0]
                : disguiseService.getRandomValue(DisguiseEntryType.NAME);
        String skin = args.length > 1
                ? args[1]
                : disguiseService.getRandomValue(DisguiseEntryType.SKIN);
        if (name == null || skin == null) {
            user.sendMessage(CC.RED + "An error occurred while trying to disguise you, contact a developer.");
            return;
        }
        if (!disguiseService.isValidMinecraftName(name)) {
            user.sendMessage(CC.RED + "You tried to disguise with an invalid minecraft name.");
            return;
        }
        if (!disguiseService.isValidMinecraftName(skin) || textureService.getOrFetch(skin) == null) {
            user.sendMessage(CC.RED + "You tried to disguise with an invalid minecraft skin.");
            return;
        }
        if (userService.fetch(name).isPresent()) {
            user.sendMessage(CC.RED + "You cannot disguise with that name.");
            return;
        }
        disguiseService.disguise(user, name, skin, DisguiseAction.DISGUISE);
        user.sendMessage(CC.PURPLE + "[Disguise] " + CC.GRAY + "You now appear as " + Rank.DEFAULT.getColor() + name + CC.GRAY + '.');
    }
}
