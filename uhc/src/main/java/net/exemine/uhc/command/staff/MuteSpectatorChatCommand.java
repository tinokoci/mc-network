package net.exemine.uhc.command.staff;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class MuteSpectatorChatCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;

    public MuteSpectatorChatCommand(GameService gameService) {
        super(List.of("mutespectatorchat", "mutespecchat", "mutespec", "specmute"), Rank.TRIAL_MOD);
        this.gameService = gameService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        gameService.setSpectatorChatMuted(!gameService.isSpectatorChatMuted());
        user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "Players can " + StringUtil.formatBooleanCommand(gameService.isSpectatorChatMuted()) + CC.GRAY + " talk in spectator chat.");
    }
}
