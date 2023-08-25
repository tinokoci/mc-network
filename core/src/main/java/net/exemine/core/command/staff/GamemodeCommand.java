package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import org.bukkit.GameMode;

import java.util.List;

public class GamemodeCommand extends BaseCommand<CoreUser, CoreData> {

    public GamemodeCommand() {
        super(List.of("gamemode", "gm"), Rank.ADMIN);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length == 0 || args.length > 2) {
            sendUsage(user);
            return;
        }
        CoreUser target = args.length == 1
                ? user
                : userService.get(args[1]);
        if (isUserOffline(user, target)) return;

        switch (args[0].toLowerCase()) {
            case "creative":
            case "c":
            case "1":
                updateGamemode(user, target, GameMode.CREATIVE);
                break;
            case "survival":
            case "s":
            case "0":
                updateGamemode(user, target, GameMode.SURVIVAL);
                break;
            case "adventure":
            case "a":
            case "2":
                updateGamemode(user, target, GameMode.ADVENTURE);
                break;
            default:
                sendUsage(user);
        }
    }

    private void updateGamemode(CoreUser user, CoreUser target, GameMode gamemode) {
        boolean selfUpdate = user == target;
        String gamemodeName = StringUtil.formatEnumName(gamemode.name());

        if (target.getGameMode() == gamemode) {
            user.sendMessage((selfUpdate ? CC.RED + "Your" : target.getColoredDisplayName() + CC.RED + "'s") + " gamemode is already set to " + CC.BOLD + gamemodeName + CC.RED + '.');
            return;
        }
        target.setGameMode(gamemode);
        user.sendMessage(CC.PURPLE + "[Gamemode] " + CC.GRAY + "You've updated " + (selfUpdate ? "your" : target.getColoredDisplayName() + CC.GRAY + "'s") + " gamemode to " + CC.GOLD + gamemodeName + CC.GRAY + '.');

        if (!selfUpdate) {
            target.sendMessage(CC.PURPLE + "[Gamemode] " + user.getColoredRealName() + CC.GRAY + " updated your gamemode to " + CC.GOLD + gamemodeName + CC.GRAY + '.');
        }
    }

    private void sendUsage(CoreUser user) {
        user.sendMessage(CC.RED + "Usage: /gamemode <creative|survival|adventure> [user]");
    }
}
