package net.exemine.uhc.config.impl;

import net.exemine.api.util.string.CC;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.border.BorderRadius;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.game.event.ScatterStartEvent;
import org.bukkit.event.EventHandler;

public class AutoBorderListener extends ConfigListener {

    @EventHandler
    public void onScatterStart(ScatterStartEvent ignored) {
        plugin.getBorderService().shrinkBorder(getBorderRadius());
        NumberOption.BORDER_SHRINK_START.setValue(getBorderShrinkStart(), false);

        MessageUtil.send(CC.BOLD_GOLD + "[Border] " + CC.GRAY + "Since there are " + CC.PINK + plugin.getServer().getOnlinePlayers().size() + CC.GRAY +
                " players online, the border is at " + CC.WHITE + plugin.getBorderService().getCurrentRadius().getValue() + CC.GRAY +
                " and will start shrinking at " + CC.PINK + (NumberOption.BORDER_SHRINK_START.getValue() + 5) + CC.GRAY + " minutes.");
    }
    private int getBorderShrinkStart() {
        return plugin.getServer().getOnlinePlayers().size() >= 100 ? 35 : 30;
    }

    private BorderRadius getBorderRadius() {
        int online = plugin.getServer().getOnlinePlayers().size();

        if (online < 50) return BorderRadius.RADIUS_1000;
        if (online < 100) return BorderRadius.RADIUS_1500;
        return BorderRadius.RADIUS_2000;
    }
}
