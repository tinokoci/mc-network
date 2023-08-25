package net.exemine.uhc.command.staff;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.user.UHCUser;

import java.util.List;
import java.util.stream.Collectors;

public class WhitelistCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;

    public WhitelistCommand(GameService gameService) {
        super(List.of("whitelist", "wl", "uhcwl"), Rank.TRIAL_MOD);
        this.gameService = gameService;
        setUsage(CC.RED + "Usage: /whitelist <on|off|add|remove|clear|list> [player]");
        setAsync(true);
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (args.length < 1 || args.length > 2) {
            user.sendMessage(getUsage());
            return;
        }
        switch (args[0].toLowerCase()) {
            case "on":
                if (ToggleOption.WHITELIST.isEnabled()) {
                    user.sendMessage(CC.RED + "UHC whitelist is currently enabled.");
                    return;
                }
                ToggleOption.WHITELIST.setEnabled(true);
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You have " + CC.GREEN + "enabled" + CC.GRAY + " UHC whitelist.");
                break;
            case "off":
                if (ToggleOption.WHITELIST.isDisabled()) {
                    user.sendMessage(CC.RED + "UHC whitelist is currently disabled.");
                    return;
                }
                ToggleOption.WHITELIST.setEnabled(false);
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You have " + CC.RED + "disabled" + CC.GRAY + " UHC whitelist.");
                break;
            case "add":
                if (args.length != 2) {
                    user.sendMessage(getUsage());
                    return;
                }
                userService.fetch(args[1]).ifPresentOrElse(target -> {
                    if (gameService.isWhitelisted(target)) {
                        user.sendMessage(target.getColoredRealName() + CC.RED + " is already whitelisted this game.");
                        return;
                    }
                    gameService.addToWhitelist(target);
                    user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You've whitelisted " + target.getColoredRealName() + CC.GRAY + " for this game.");
                }, () -> user.sendMessage(Lang.USER_NEVER_PLAYED));
                break;
            case "remove":
                if (args.length != 2) {
                    user.sendMessage(getUsage());
                    return;
                }
                userService.fetch(args[1]).ifPresentOrElse(target -> {
                    if (!gameService.isWhitelisted(target)) {
                        user.sendMessage(target.getColoredRealName() + CC.RED + " is not whitelisted this game.");
                        return;
                    }
                    gameService.removeFromWhitelist(target);
                    user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You've removed " + target.getColoredRealName() + CC.GRAY + " from this game's whitelist.");
                }, () -> user.sendMessage(Lang.USER_NEVER_PLAYED));
                break;
            case "clear":
                if (gameService.getWhitelistedUsers().isEmpty()) {
                    user.sendMessage(CC.RED + "There are no whitelisted players this game.");
                    return;
                }
                gameService.clearWhitelist();
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You've cleared this game's whitelisted players.");
                break;
            case "list":
                if (gameService.getWhitelistedUsers().isEmpty()) {
                    user.sendMessage(CC.RED + "There are no whitelisted players this game.");
                    return;
                }
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "Whitelisted players: " + StringUtil.listToString(gameService.getWhitelistedUsers().stream().map(UHCUser::getColoredRealName).collect(Collectors.toList()), CC.GRAY));
                break;
            default:
                user.sendMessage(getUsage());
        }
    }
}
