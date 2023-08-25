package net.exemine.uhc.scatter;

import lombok.Getter;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class ScatterTask extends BukkitRunnable {

    private final GameService gameService;
    private final UHCUserService userService;

    private final List<UUID> userUuidsToScatter;
    private final int usersPerTick;

    private final int startValue;
    private int countdown;

    public ScatterTask(UHC plugin, int countdown) {
        this.gameService = plugin.getGameService();
        this.userService = plugin.getUserService();
        this.userUuidsToScatter = userService.getWaitingUsers().stream().map(UHCUser::getUniqueId).collect(Collectors.toList());
        this.usersPerTick =
            (int) Math.ceil((double) userUuidsToScatter.size() / (double) countdown) + 1;
        this.startValue = countdown;
        this.countdown = startValue;
        runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    public void run() {
        if (--countdown == 0) {
            gameService.startGame();
            cancel();
            return;
        }
        for (int i = 0; i < usersPerTick; i++) {
            if (userUuidsToScatter.isEmpty()) break;

            UHCUser userToScatter = userService.get(userUuidsToScatter.remove(0));
            if (userToScatter == null) continue;
            userToScatter.setState(UHCUserState.SCATTER);
        }
        if (TimeUtil.shouldAlert(countdown, startValue)) {
            MessageUtil.send(CC.BOLD_GOLD + "[Start] " + CC.GRAY + "The game is starting in " + CC.PINK + countdown + CC.GRAY + " second" + (countdown == 1 ? "" : "s") + '.', Sound.CLICK);
        }
    }
}
