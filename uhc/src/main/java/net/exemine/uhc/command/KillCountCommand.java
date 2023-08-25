package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.user.UHCUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class KillCountCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;

    public KillCountCommand(GameService gameService) {
        super(List.of("killcount", "kc"));
        this.gameService = gameService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (!gameService.isStateOrHigher(GameState.PLAYING)) {
            user.sendMessage(CC.RED + "You cannot do that in the current game state.");
            return;
        }
        UHCUser target = args.length == 0 ? user : userService.get(args[0]);
        if (isUserOffline(user, target)) return;

        LinkedHashMap<Long, UUID> killedUsers = target.getGameInfo().getKilledUsers();
        boolean hasKilledUserData = user.isEqualOrAbove(RankType.STAFF) && !killedUsers.isEmpty();

        if (hasKilledUserData) user.sendMessage();
        user.sendMessage(CC.PURPLE + "[Game] " + (user == target ? CC.GRAY + "You have " : target.getColoredDisplayName() + CC.GRAY + " has ")
                + CC.GOLD + target.getGameInfo().getKills().getValue() + CC.GRAY + " kills.");

        if (hasKilledUserData) {
            killedUsers.forEach((timestamp, uuid) -> {
                UHCUser killedUser = userService.retrieve(uuid);
                String time = TimeUtil.getNormalDuration(System.currentTimeMillis() - timestamp);
                user.sendMessage(' ' + CC.GRAY + Lang.BULLET + ' ' + killedUser.getColoredDisplayName() + ' ' + CC.PINK + '(' + time + " ago)");
            });
            user.sendMessage();
        }
    }
}
