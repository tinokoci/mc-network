package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class ThanksCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;

    public ThanksCommand(GameService gameService) {
        super(List.of("thanks", "ty4h"));
        this.gameService = gameService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (gameService.isState(GameState.WORLD_GENERATION, GameState.LOBBY)) {
            user.sendMessage(CC.RED + "You cannot do that in the current game state.");
            return;
        }
        if (user.isHostThanks()) {
            user.sendMessage(CC.RED + "You've already thanked the host.");
            return;
        }
        user.setHostThanks(true);
        MessageUtil.send(user.getColoredDisplayName() + CC.BOLD_YELLOW + " has thanked the host!");
    }
}
