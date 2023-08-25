package net.exemine.uhc.provider;

import lombok.RequiredArgsConstructor;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.provider.BossBarProvider;
import net.exemine.core.provider.bossbar.BossBar;
import net.exemine.uhc.autostart.AutoStartTask;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.user.UHCUser;

@RequiredArgsConstructor
public class UHCBossBarProvider implements BossBarProvider<UHCUser> {

    private final GameService gameService;

    @Override
    public void setup(UHCUser user, BossBar<UHCUser> bossBar) {
        bossBar.setSecondsPerElement(5);
    }

    @Override
    public void update(UHCUser user, BossBar<UHCUser> bossBar) {
        if (gameService.isState(GameState.LOBBY) && gameService.isScheduledToAutoStart()) {
            AutoStartTask task = gameService.getAutoStartTask();
            float percentage = task.getCountdown() / (float) task.getStartValue();
            bossBar.add(CC.BOLD_PINK + "Starting In: " + CC.BOLD_WHITE + TimeUtil.getClockTime(task.getCountdown()), percentage);
        }
        if (gameService.isState(GameState.PLAYING)) {
            gameService.getTask().getTimedActions()
                    .stream()
                    .filter(timedAction -> !timedAction.isActivated())
                    .forEach(timedAction -> {
                        float percentage = (timedAction.getOption().getMinutesInSeconds() - ((System.currentTimeMillis() - gameService.getStartTime()) / 1000f)) / (float) timedAction.getOption().getMinutesInSeconds();
                        bossBar.add(CC.BOLD_PINK + timedAction.getOption().getName() + ": " + CC.BOLD_WHITE + TimeUtil.getClockTime(timedAction.getCountdown()), percentage);
                    });
        }
    }
}
