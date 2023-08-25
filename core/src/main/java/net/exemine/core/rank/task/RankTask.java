package net.exemine.core.rank.task;

import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.permission.PermissionService;
import net.exemine.api.rank.RankService;
import net.exemine.core.Core;
import net.exemine.core.rank.event.RankChangeEvent;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class RankTask extends BukkitRunnable {

    private final BulkDataService bulkDataService;
    private final RankService rankService;
    private final PermissionService permissionService;
    private final UserService<CoreUser, CoreData> userService;

    public RankTask(Core plugin) {
        this.bulkDataService = plugin.getBulkDataService();
        this.rankService = plugin.getRankService();
        this.permissionService = plugin.getPermissionService();
        this.userService = plugin.getUserService();

        subscribeToRankUpdate();
        subscribeToPermissionUpdates();
        runTaskTimerAsynchronously(plugin, 0, 20L);
    }

    @Override
    public void run() {
        userService.getOnlineUsers().forEach(user -> user.getBulkData().getRankInfoList()
                .stream()
                .filter(rankInfo -> rankInfo.isExpired() && !rankInfo.isRemoved())
                .forEach(rankInfo -> rankService.removeRank(user.getCoreData(), user.getBulkData(), rankInfo, null)));
    }

    private void subscribeToRankUpdate() {
        rankService.subscribeToRankUpdate(model -> {
            CoreUser user = userService.get(model.getUniqueId());

            if (user != null) {
                bulkDataService.loadRanks(user.getBulkData());
                user.setupPowers();

                RankChangeEvent rankChangeEvent = new RankChangeEvent(user, user.getRank(), model.isMainRankChanged());
                Bukkit.getPluginManager().callEvent(rankChangeEvent);
            }
        });
    }

    private void subscribeToPermissionUpdates() {
        permissionService.subscribeToPermissionsUpdate(model -> {
            CoreUser user = userService.get(model.getUniqueId());
            if (user == null) return;
            bulkDataService.loadPermissions(user.getBulkData());
        });
    }
}
