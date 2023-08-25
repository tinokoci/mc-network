package net.exemine.core.server.task;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@Setter
@Getter
public class RebootTask extends BukkitRunnable {

    private final UserService<CoreUser, CoreData> userService;

    private int countdown;

    public RebootTask(Core plugin, long countdown) {
        this.userService = plugin.getUserService();
        this.countdown = ((int) countdown / 1000) + 1;
        runTaskTimer(plugin, 20L, 20L);
        run();
    }

    @Override
    public void run() {
        countdown--;

        if (countdown <= 5) {
            String message = CC.RED + "You were sent to the hub because the previous instance you were on was scheduled to restart.";
            userService.getOnlineUsers().forEach(user -> user.sendToHub(message));
        }
        if (countdown <= 0) {
            String message = CC.RED + "You were kicked because the previous instance you were on was scheduled to restart.";
            userService.getOnlineUsers().forEach(user -> user.kickPlayer(message));
            Bukkit.shutdown();
            return;
        }
        if (TimeUtil.shouldAlert(countdown)) {
            MessageUtil.send(CC.RED + "This server will automatically restart in " + TimeUtil.getNormalDuration(countdown) + '.');
        }
    }
}
