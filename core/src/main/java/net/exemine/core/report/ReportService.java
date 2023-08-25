package net.exemine.core.report;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.RankType;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.redis.pubsub.model.generic.StringModel;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.report.type.ReportType;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.InstanceUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class ReportService {

    private final UserService<CoreUser, CoreData> userService;
    private final RedisService redisService;

    private final Multimap<UUID, Report> reports = HashMultimap.create();
    private final Map<UUID, Long> reportCooldown = new HashMap<>();

    @Setter
    private ReportProvider reportProvider;

    public ReportService(UserService<CoreUser, CoreData> userService, RedisService redisService) {
        this.userService = userService;
        this.redisService = redisService;
        subscribeToPlayerReport();
    }

    public boolean canReport(CoreUser user) {
        long cooldown = reportCooldown.getOrDefault(user.getUniqueId(), -1L);
        boolean check = cooldown != -1 && cooldown > System.currentTimeMillis();

        if (check) {
            user.sendMessage(CC.RED + "Please wait " + CC.BOLD + TimeUtil.getNormalDuration(cooldown - System.currentTimeMillis() + 1000) + CC.RED + " before making a new report.");
        }
        return !check;
    }

    public void addReport(CoreUser issuer, CoreUser target, ReportType type) {
        reports.get(UUID.randomUUID());
        reportCooldown.put(issuer.getUniqueId(), System.currentTimeMillis() + 1000 * 60);

        Report newReport = new Report(issuer.getRealName(), target, type);
        Report previousReport = reports.values().stream().filter(report -> report.match(newReport)).findAny().orElse(null);

        // If player spam reports someone, just replace the previous report with the newest
        if (previousReport != null) {
            reports.remove(target.getUniqueId(), previousReport);
        }
        reports.put(target.getUniqueId(), newReport);

        issuer.sendMessage(CC.GREEN + "You have submitted a report.");
        redisService.getPublisher().sendPlayerReport(CC.DARK_RED + "[Report] " + CC.GRAY + '[' + InstanceUtil.getName() + "] " + issuer.getColoredDisplayName() + CC.RED + " has reported " + target.getColoredDisplayName() + CC.RED + " for " + CC.GRAY + type.getName());
    }

    public void removeReports(UUID uuid) {
        reports.removeAll(uuid);
    }

    private void subscribeToPlayerReport() {
        redisService.subscribe(RedisMessage.PLAYER_REPORT, StringModel.class, model ->
                userService.getOnlineUsers()
                        .stream()
                        .filter(user -> user.isEqualOrAbove(RankType.STAFF) && user.getStaffData().isReports())
                        .forEach(user -> user.sendMessage(model.getMessage()))
        );
    }
}

