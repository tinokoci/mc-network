package net.exemine.uhc.game.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.callable.Callback;
import net.exemine.api.util.callable.TypeCallback;
import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.task.BorderShrinkTask;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.event.GracePeriodEndEvent;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameTask extends BukkitRunnable {

    private final GameService gameService;

    @Getter
    private final LinkedList<TimedAction> timedActions;

    public GameTask(UHC plugin) {
        gameService = plugin.getGameService();

        timedActions = new LinkedList<>(List.of(
                new TimedAction(NumberOption.FINAL_HEAL,
                        () -> {
                            plugin.getUserService().getOnlineUsers().forEach(user -> {
                                user.setHealth(20);
                                user.setFoodLevel(20);
                            });
                            Core.get().getServerService().setChatMuted(false);
                            MessageUtil.send(CC.GREEN + "You've received the final heal.", Sound.ENDERDRAGON_GROWL);
                        }, countdown -> MessageUtil.send(CC.BOLD_GOLD + "[Event] " + CC.GRAY + "The final heal will take place in " + TimeUtil.getNormalDuration(countdown, CC.PINK, CC.GRAY) + '.', Sound.CLICK)
                ),
                new TimedAction(NumberOption.GRACE_PERIOD,
                        () -> {
                            gameService.setPvP(true);
                            MessageUtil.send(CC.GREEN + "Grace period has ended, PvP is now enabled!", Sound.ENDERDRAGON_GROWL);
                            plugin.getServer().getPluginManager().callEvent(new GracePeriodEndEvent());
                        }, countdown -> MessageUtil.send(CC.BOLD_GOLD + "[Event] " + CC.GRAY + "The grace period will end in " + TimeUtil.getNormalDuration(countdown, CC.PINK, CC.GRAY) + '.', Sound.CLICK)
                ),
                new TimedAction(NumberOption.BORDER_SHRINK_START,
                        () -> {
                            //MessageUtil.send(CC.GREEN + "The border will shrink every " + NumberOption.BORDER_SHRINK_INTERVAL.getValue() + " minutes from now on. Good luck everyone!", Sound.ENDERDRAGON_GROWL);
                            new BorderShrinkTask(plugin, plugin.getBorderService().getNextBorderRadius());
                        }, countdown -> {
                    //MessageUtil.send(CC.BOLD_GOLD + "[Event] " + CC.GRAY + "The border will start shrinking in " + TimeUtil.getNormalDuration(countdown, CC.PINK, CC.GRAY) + '.', Sound.CLICK);
                }
                )
        ));
        runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    public void run() {
        // Don't alert multiple actions at the same time
        AtomicBoolean alert = new AtomicBoolean();

        timedActions.forEach(timedAction -> {
            if (timedAction.isActivated()) return;

            if (timedAction.shouldActivate()) {
                timedAction.getExecuteCallback().run();
                timedAction.setActivated(true);
                alert.set(true);
                return;
            }
            int countdown = timedAction.getCountdown();
            if (!alert.get() && TimeUtil.shouldAlert(countdown, timedAction.getOption().getMinutesInSeconds())) {
                timedAction.getAlertCallback().run(countdown);
                alert.set(true);
            }
        });
    }

    @RequiredArgsConstructor
    @Getter
    @Setter
    public class TimedAction {

        private final NumberOption option;
        private final Callback executeCallback;
        private final TypeCallback<Integer> alertCallback;
        private boolean activated;

        public boolean shouldActivate() {
            int secondsNeededToActivate = option.getValue() * 60;
            int secondsSinceStartTime = (int) ((System.currentTimeMillis() - gameService.getStartTime()) / 1000L);
            return secondsNeededToActivate <= secondsSinceStartTime;
        }

        public int getCountdown() {
            int secondsNeededToActivate = option.getValue() * 60;
            int secondsSinceStartTime = (int) ((System.currentTimeMillis() - gameService.getStartTime()) / 1000L);
            return secondsNeededToActivate - secondsSinceStartTime;
        }
    }
}
