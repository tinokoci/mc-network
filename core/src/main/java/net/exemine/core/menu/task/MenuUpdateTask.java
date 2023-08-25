package net.exemine.core.menu.task;

import net.exemine.core.Core;
import net.exemine.core.menu.Menu;
import org.bukkit.scheduler.BukkitRunnable;

public class MenuUpdateTask extends BukkitRunnable {

    public MenuUpdateTask() {
        runTaskTimerAsynchronously(Core.get(), 0L, 10L);
    }

    @Override
    public void run() {
        Menu.OPENED_MENUS.values()
                .stream()
                .filter(menu -> menu.getUser() != null
                        && menu.getUser().isOnline()
                        && menu.isAutoUpdate())
                .forEach(Menu::open);
    }
}