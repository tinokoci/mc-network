package net.exemine.uhc.game.task;

import net.exemine.api.util.Executor;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.MessageUtil;
import net.exemine.core.util.ServerUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class EndTask extends BukkitRunnable {

    private final UHC plugin;

    private final int startValue = 300;
    private int countdown = startValue;

    public EndTask(UHC plugin) {
        this.plugin = plugin;
        runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    public void run() {
        UHCUserService userService = plugin.getUserService();

        if (--countdown == 0) {
            String kickMessage = CC.RED + "This UHC game has finished! Thanks everyone for participating.";

            userService.getOnlineUsers().forEach(user -> user.sendToHub(kickMessage));

            Executor.schedule(() -> {
                plugin.setShuttingDown(true);
                userService.getOnlineUsers().forEach(user -> user.kickPlayer(kickMessage));
                Executor.schedule(Bukkit::shutdown).runSyncLater(10_000L);
            }).runSyncLater(3000L);
        }

        // Spawn fireworks for first 10 seconds
        if (countdown > startValue - 10) {
            plugin.getTeamService().getAliveTeams().get(0).getMembers()
                    .stream()
                    .filter(UHCUser::isPlaying)
                    .forEach(user -> ServerUtil.launchFirework(user.getLocation(), Color.GREEN, 2, true, true));
        }
        if (TimeUtil.shouldAlert(countdown, startValue, 45)) {
            MessageUtil.send(CC.BOLD_GOLD + "[Event] " + CC.GRAY + "The game will end in " + TimeUtil.getNormalDuration(countdown, CC.PINK, CC.GRAY) + '.', Sound.CLICK);
        }
    }
}
