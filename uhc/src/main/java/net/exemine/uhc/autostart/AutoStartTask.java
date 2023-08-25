package net.exemine.uhc.autostart;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import net.exemine.core.util.InstanceUtil;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.scenario.Scenario;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public class AutoStartTask extends BukkitRunnable {

    private final UHC plugin;
    private boolean removedWhitelist;

    @Setter
    @Getter
    private long timestamp;

    @Getter
    private final int startValue;

    @Getter
    private int countdown;

    public AutoStartTask(UHC plugin, long timestamp) {
        this.plugin = plugin;
        this.timestamp = timestamp;
        this.startValue = calculateCountdown();
        this.countdown = startValue;
        runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    public void run() {
        countdown = calculateCountdown();

        if (ToggleOption.WHITELIST.isEnabled() && !removedWhitelist &&
                Instant.now().isAfter(Instant.ofEpochMilli(timestamp).minus(10L, ChronoUnit.MINUTES))) {
            ToggleOption.WHITELIST.setEnabled(false);
            String scenarios = Scenario.getEnabledScenarios().isEmpty()
                    ? "Vanilla"
                    : Scenario.getEnabledScenarios().stream().map(scenario -> scenario.getName().toString()).collect(Collectors.joining(", "));
            Core.get().getRedisService().getPublisher().sendAlertUHCAnnounce(
                    CC.BOLD_PURPLE + "UHC " + plugin.getGameService().getFormattedMode()
                            + CC.ITALIC_GRAY + ' ' + '(' + scenarios + ')' + CC.GOLD + " is no longer whitelisted and will start in " + CC.PINK + 10 + CC.GOLD + " minutes! ",
                    CC.BOLD_GREEN + "Click to play!",
                    CC.GREEN + "Click to play UHC!",
                    "/join " + InstanceUtil.getName()
            );
            removedWhitelist = true;
            return;
        }
        if (countdown <= 0) {
            plugin.getScatterService().startScatter(60);
            return;
        }
        if (TimeUtil.shouldAlert(countdown, 60 * 10)) {
            MessageUtil.send(CC.BOLD_GOLD + "[Start] " + CC.GRAY + "The scatter will start in " + TimeUtil.getNormalDuration(countdown, CC.PINK, CC.GRAY) + '.', Sound.CLICK);
        }
    }

    private int calculateCountdown() {
        return (int) ((timestamp - System.currentTimeMillis()) / 1000L);
    }
}
