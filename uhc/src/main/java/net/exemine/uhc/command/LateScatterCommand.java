package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserState;

import java.util.List;

public class LateScatterCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;

    public LateScatterCommand(GameService gameService) {
        super(List.of("latescatter", "latejoin"));
        this.gameService = gameService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (gameService.isNotState(GameState.PLAYING)) {
            user.sendMessage(CC.RED + "You cannot do that in the current game state.");
            return;
        }
        if (user.isScattering()) {
            user.sendMessage(CC.RED + "You're currently being scattered.");
            return;
        }
        if (user.isPlaying()) {
            user.sendMessage(CC.RED + "You're already playing in this game.");
            return;
        }
        if (user.getGameInfo().isDied()) {
            user.sendMessage(CC.RED + "You've already participated in this game.");
            return;
        }
        if (!user.canLateScatter()) {
            user.sendMessage(CC.RED + "Your time to late scatter has expired.");

            if (user.hasNoRank()) {
                user.sendMessage(CC.ITALIC_GRAY + "(Buy a rank at " + CC.BOLD + Lang.STORE + CC.ITALIC_GRAY + " to increase this time.)");
            }
            return;
        }
        gameService.setInitialPlayers(gameService.getInitialPlayers() + 1);
        user.setState(UHCUserState.SCATTER);
        Executor.schedule(() -> {
            user.setState(UHCUserState.IN_GAME);
            user.sendMessage(CC.GREEN + "You've been late scattered.");
        }).runSyncLater(3000L);
    }
}
