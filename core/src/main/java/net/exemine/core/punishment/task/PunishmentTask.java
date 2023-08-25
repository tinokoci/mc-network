package net.exemine.core.punishment.task;

import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.punishment.Punishment;
import net.exemine.api.punishment.PunishmentService;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.Core;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.scheduler.BukkitRunnable;

public class PunishmentTask extends BukkitRunnable {

    private final BulkDataService bulkDataService;
    private final PunishmentService punishmentService;
    private final UserService<CoreUser, CoreData> userService;

    public PunishmentTask(Core plugin) {
        this.bulkDataService = plugin.getBulkDataService();
        this.punishmentService = plugin.getPunishmentService();
        this.userService = plugin.getUserService();

        subscribeToPunishmentExecutions();
        subscribeToPunishmentUpdates();
        runTaskTimerAsynchronously(plugin, 0, 10L);
    }

    @Override
    public void run() {
        userService.getOnlineUsers().forEach(user -> user.getBulkData().getActivePunishments()
                .stream()
                .filter(Punishment::isExpired)
                .forEach(punishment -> punishmentService.removePunishment(punishment, null, "Expired")));
    }

    private void subscribeToPunishmentExecutions() {
        punishmentService.subscribeToPunishmentExecution(model -> userService.fetch(model.getUniqueId()).ifPresent(user -> {
            BulkData data = user.getBulkData();
            bulkDataService.loadPunishments(data);
            Punishment punishment = data.getPunishmentByIndex(model.getType(), model.getIndex());

            // Kick active alt accounts if the punishment is an IP BAN
            if (punishment.getType().isOrGreaterThan(PunishmentType.IP_BAN)) {
                user.getAltAccounts(false)
                        .stream()
                        .filter(CoreUser::isOnline)
                        .forEach(alt -> Executor.schedule(() -> alt.getPlayer().kickPlayer(punishment.getLoginMessage())).runSync());
            }
            if (user.isOffline()) return;

            if (punishment.getType() == PunishmentType.MUTE) {
                user.sendMessage(CC.RED + "You have been " + (punishment.isPermanent() ? "permanently" : "temporarily")
                        + " muted for '" + punishment.getAddedReason() + "'.\nAppeal at " + Lang.DISCORD);
                return;
            }
            Executor.schedule(() -> user.kickPlayer(punishment.getLoginMessage())).runSync();

            if (punishment.getType() == PunishmentType.KICK) {
                punishmentService.removePunishment(punishment, null, "Kick");
            }
        }));
    }

    private void subscribeToPunishmentUpdates() {
        punishmentService.subscribeToPunishmentUpdate(model -> {
            CoreUser user = userService.get(model.getUniqueId());
            if (user == null) return;

            BulkData data = user.getBulkData();
            bulkDataService.loadPunishments(data);
        });
    }
}