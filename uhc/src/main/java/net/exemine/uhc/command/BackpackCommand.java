package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.scenario.Scenario;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class BackpackCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;

    public BackpackCommand(GameService gameService) {
        super(List.of("backpack", "bp"));
        this.gameService = gameService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (gameService.isSoloGame()) {
            user.sendMessage(CC.RED + "This cannot be used in a solo game.");
            return;
        }
        if (Scenario.BACKPACKS.isDisabled()) {
            user.sendMessage(CC.RED + "This command is currently disabled because " + CC.BOLD + "Backpack" + CC.RED + " scenario is disabled.");
            return;
        }
        if (!user.isPlaying() && !user.isGameModerator()) {
            user.sendMessage(CC.RED + "You cannot do that in your current state.");
            return;
        }
        if (!user.isGameModerator()) {
            user.openInventory(user.getTeam().getBackpack());
            user.sendMessage(CC.PURPLE + "[Backpack] " + CC.GRAY + "You've opened your team's backpack.");
            return;
        }
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /backpack <player>");
            return;
        }
        UHCUser target = userService.get(args[0]);
        if (isUserOffline(user, target)) return;

        user.openInventory(target.getTeam().getBackpack());
        user.sendMessage(CC.PURPLE + "[Backpack] " + CC.GRAY + "You're viewing the backpack of " + target.getColoredDisplayName() + CC.GRAY + "'s team.");
    }
}
