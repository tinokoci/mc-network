package net.exemine.uhc.command.staff;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserState;

import java.util.List;

public class SpectatorChatCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;

    public SpectatorChatCommand(GameService gameService) {
        super(List.of("spectatorchat", "specchat"), Rank.TRIAL_MOD);
        this.gameService = gameService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (gameService.isState(GameState.WORLD_GENERATION, GameState.LOBBY)) {
            user.sendMessage(CC.RED + "You cannot do that in the current game state.");
            return;
        }
        if (!user.isGameModerator()) {
            if (user.inState(UHCUserState.SPECTATOR)) {
                user.sendMessage(CC.RED + "You're in a state where this option is enabled as default.");
                return;
            }
            user.sendMessage(CC.RED + "You cannot speak in " + CC.BOLD + "spectator" + CC.RED + " chat if you're not moderating the game.");
            return;
        }
        if (args.length != 0) {
            if (gameService.isSpectatorChatMuted()) {
                user.sendMessage(CC.RED + "Spectator chat is muted at the moment.");
                return;
            }
            if (!user.getStaffData().isSpectatorChatMessages()) {
                user.sendMessage(CC.RED + "You're trying to speak in the " + CC.BOLD + "spectator" + CC.RED + " chat, but have disabled these messages in your settings.");
                return;
            }
            String message = CC.GRAY + "[Spectator] " + user.getState().getStaffLongPrefix() + user.getFullDisplayName() + CC.GRAY + ": " + StringUtil.join(args);
            userService.getOnlineUsers()
                    .stream()
                    .filter(onlineUser -> onlineUser.isSpectating() || gameService.isState(GameState.ENDING))
                    .forEach(onlineUser -> onlineUser.sendMessage(message));
            return;
        }
        user.setSpectatorChat(!user.isSpectatorChat());
        user.sendMessage(CC.PURPLE + "[Chat] " + CC.GRAY + "You're " + StringUtil.formatBooleanCommand(user.isSpectatorChat()) + CC.GRAY + " speaking in " + CC.GOLD + "spectator " + CC.GRAY + "chat.");
    }
}
