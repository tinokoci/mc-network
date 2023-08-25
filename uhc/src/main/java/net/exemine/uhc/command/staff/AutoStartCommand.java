package net.exemine.uhc.command.staff;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.UHC;
import net.exemine.uhc.autostart.AutoStartTask;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class AutoStartCommand extends BaseCommand<UHCUser, UHCData> {

    private final UHC plugin;
    private final GameService gameService;

    public AutoStartCommand(UHC plugin) {
        super(List.of("autostart"), Rank.TRIAL_MOD);
        this.plugin = plugin;
        this.gameService = plugin.getGameService();
        setUsage(CC.RED + "Usage: /autostart <time|cancel>");
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (!user.canExecuteModCommand()) {
            user.sendMessage(CC.RED + "You cannot do that in your current state.");
            return;
        }
        if (gameService.isNotState(GameState.LOBBY)) {
            user.sendMessage(CC.RED + "You cannot do that in the current game state.");
            return;
        }
        if (args.length != 1) {
            user.sendMessage(getUsage());
            return;
        }
        if (args[0].equalsIgnoreCase("cancel")) {
            if (!gameService.isScheduledToAutoStart()) {
                user.sendMessage(CC.RED + "This game is not scheduled to auto start.");
                return;
            }
            gameService.cancelAutoStart();
            user.sendMessage(CC.RED + "You've cancelled the auto start.");
            return;
        }
        long millis = TimeUtil.getMillisFromInput(args[0]);

        if (millis == Long.MAX_VALUE) {
            user.sendMessage(getUsage());
            return;
        }
        boolean reschedule = gameService.isScheduledToAutoStart();

        if (reschedule) {
            gameService.cancelAutoStart();
        }
        long timestamp = System.currentTimeMillis() + millis;
        gameService.setAutoStartTask(new AutoStartTask(plugin, timestamp));
        user.sendMessage(CC.PURPLE + "[Start] " + CC.GRAY + "You've " + (reschedule ? "re" : "") + "scheduled the game to auto start in " + TimeUtil.getNormalDuration(millis, CC.PINK, CC.GRAY) + '.');
    }
}
