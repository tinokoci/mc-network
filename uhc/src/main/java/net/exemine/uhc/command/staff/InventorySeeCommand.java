package net.exemine.uhc.command.staff;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.spectator.menu.InventoryViewerMenu;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class InventorySeeCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;

    public InventorySeeCommand(GameService gameService) {
        super(List.of("inventorysee", "invsee", "inventory", "inv"));
        this.gameService = gameService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (!user.isEqualOrAbove(RankType.STAFF) && gameService.isNotState(GameState.ENDING)) {
            user.sendMessage(Lang.NO_PERMISSION);
            return;
        }
        if (!user.isGameModerator() && gameService.isNotState(GameState.ENDING)) {
            user.sendMessage(CC.RED + "You cannot do that in the current game state.");
            return;
        }
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /invsee <player>");
            return;
        }
        UHCUser target = userService.get(args[0]);
        if (isUserOffline(user, target)) return;

        new InventoryViewerMenu(user, target).open();
        user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You're now viewing " + target.getColoredDisplayName() + CC.GRAY + "'s inventory.");
    }
}
