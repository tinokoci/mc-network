package net.exemine.uhc.command.staff;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.util.LocationUtil;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public class TeleportCommand extends BaseCommand<UHCUser, UHCData> {

    public TeleportCommand() {
        super(List.of("teleport", "tp", "tele"), Rank.TRIAL_MOD);
        setUsage(CC.RED + "Usage: /teleport <player> [target] or <x> <y> <z> [world]");
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (!user.canExecuteModCommand()) {
            user.sendMessage(CC.RED + "You cannot do that in your current state.");
            return;
        }
        switch (args.length) {
            case 1:
                UHCUser target = userService.get(args[0]);
                if (isUserOffline(user, target)) return;

                if (target == user) {
                    user.sendMessage(CC.RED + "You can't teleport to yourself.");
                    return;
                }
                if (cannotTeleport(user, target.getLocation())) return;

                user.teleport(target);
                user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You've teleported to " + target.getColoredDisplayName() + CC.GRAY + '.');
                break;
            case 2:
                UHCUser target1 = userService.get(args[0]);
                UHCUser target2 = userService.get(args[1]);
                if (isUserOffline(user, target1) || isUserOffline(user, target2)) return;

                if (target1 == user) {
                    user.performCommand("teleport " + args[1]);
                    return;
                }
                if (target1 == target2) {
                    user.sendMessage(CC.RED + "You can't teleport a player to himself.");
                    return;
                }
                boolean teleportToUser = target2 == user;

                target1.teleport(target2);
                target1.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You've been teleported to " + target2.getColoredDisplayName() + (teleportToUser ? "" : CC.GRAY + " by " + user.getColoredRealName()) + CC.GRAY + '.');
                user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You've teleported " + target1.getColoredDisplayName() + CC.GRAY + " to " + (teleportToUser ? "yourself" : target2.getColoredDisplayName()) + CC.GRAY + '.');
                break;
            case 3:
            case 4:
                if (!StringUtil.isDouble(args[0], args[1], args[1])) {
                    user.sendMessage(getUsage());
                    return;
                }
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);

                World world = user.getWorld();

                if (args.length == 4) {
                    World inputWorld = Bukkit.getWorld(args[3]);
                    if (inputWorld != null) world = inputWorld;
                }
                Location location = new Location(world, x, y, z);
                if (cannotTeleport(user, location)) return;

                user.teleport(location);
                user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You've teleported to " + CC.GOLD + '(' + x + ", " + y + ", " + z + ')' + CC.GRAY + '.');
                break;
            default:
                user.sendMessage(getUsage());
        }
    }

    // Admins bypass UHCUser#canExecuteModCommand
    private boolean cannotTeleport(UHCUser user, Location location) {
        if (user.isGameModerator()
                || user.inWorld(WorldService.LOBBY_WORLD_NAME)
                || user.inWorld(WorldService.PRACTICE_WORLD_NAME))
            return false;

        boolean cannotTeleport = LocationUtil.isOutsideRadius(location, 100);
        if (cannotTeleport) {
            user.sendMessage(CC.RED + "You cannot teleport outside 100x100 while being a regular spectator.");
        }
        return cannotTeleport;
    }
}
