package net.exemine.uhc.border.task;

import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.BorderRadius;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.config.option.NumberOption;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class BorderShrinkTask extends BukkitRunnable {

    private final UHC plugin;
    private final BorderRadius radius;

    private final int startValue;
    private int countdown;

    public BorderShrinkTask(UHC plugin, BorderRadius radius, int startValue) {
        this.plugin = plugin;
        this.radius = radius;
        this.startValue = startValue;
        this.countdown = startValue;
        plugin.getBorderService().setBorderShrinkTask(this);
        runTaskTimer(plugin, 20L, 20L);
    }

    public BorderShrinkTask(UHC plugin, BorderRadius radius) {
        this(plugin, radius, NumberOption.BORDER_SHRINK_INTERVAL.getMinutesInSeconds());
    }

    @Override
    public void run() {
        BorderService borderService = plugin.getBorderService();

        if (--countdown == 0) {
            cancel();
            borderService.setFormattedShrinkIn(null);
            boolean success = borderService.shrinkBorder(radius);

            if (success) {
                MessageUtil.send(CC.BOLD_GOLD + "[Border] " + CC.GRAY + "The border has shrunk to " + CC.WHITE + radius.getValue() + CC.GRAY + ".", Sound.ENDERDRAGON_GROWL);
                BorderRadius nextBorderRadius = borderService.getNextBorderRadius();

                if (nextBorderRadius == null) {
                    borderService.setBorderShrinkTask(null);
                    return;
                }
                borderService.setFirstShrinkOccurred(true);
                new BorderShrinkTask(plugin, nextBorderRadius);
            }
        }
        if (TimeUtil.shouldAlert(countdown, startValue)) {
            MessageUtil.send(CC.BOLD_GOLD + "[Border] " + CC.GRAY + "The border will shrink to " + CC.WHITE + radius.getValue() + CC.GRAY + " in " + TimeUtil.getNormalDuration(countdown, CC.PINK, CC.GRAY) + '.', Sound.CLICK);
        }
        borderService.setFormattedShrinkIn(TimeUtil.getCharDuration(countdown));
    }
}
