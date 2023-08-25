package net.exemine.core.report.menu;

import com.google.common.collect.Multimap;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.menu.confirm.ConfirmMenu;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.report.Report;
import net.exemine.core.report.ReportService;
import net.exemine.core.report.type.ReportType;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportViewMenu extends PaginatedMenu<CoreUser> {

    private final ReportService reportService;

    public ReportViewMenu(CoreUser user, ReportService reportService) {
        super(user, CC.DARK_GRAY + "Report Lists", 5, 3);
        this.reportService = reportService;
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addExitItem();

        if (reportService.getReports().isEmpty()) {
            set(22, new ItemBuilder(Material.WOOL)
                    .setDurability(ItemUtil.getGreen())
                    .setName(CC.GREEN + "There are no reports!")
                    .addLore(CC.GRAY + "Thanks for keeping the server clean from cheaters.")
                    .build());
        }
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();
        Multimap<UUID, Report> reportMultimap = reportService.getReports();

        reportMultimap.keys()
                .stream()
                .sorted(Comparator.comparingInt(key -> reportMultimap.get(key).size()))
                .distinct()
                .forEach(key -> {
                    Collection<Report> reports = reportMultimap.get(key);
                    if (reports.isEmpty()) return;

                    List<String> lore = new ArrayList<>();
                    Report lastReport = reports
                            .stream()
                            .max(Comparator.comparingLong(Report::getAddedAt))
                            .get();

                    lore.add(CC.DARK_GRAY + "(" + TimeUtil.getNormalDuration(System.currentTimeMillis() - lastReport.getAddedAt()) + " ago)");
                    lore.add("");
                    lore.add(CC.GRAY + "Reported for:");

                    Arrays.stream(ReportType.values())
                            .sorted(Comparator.comparing(type -> (int) reports.stream().filter(report -> report.getType() == type).count(), Comparator.reverseOrder()))
                            .forEach(type -> {
                                int count = (int) reports.stream().filter(report -> report.getType() == type).count();
                                if (count == 0) return;

                                lore.add(CC.GRAY + ' ' + Lang.BULLET + ' ' + CC.WHITE + type.getName() + CC.GRAY + " (" + CC.WHITE + count + CC.GRAY + ')');
                            });
                    lore.add("");

                    if (reportService.getReportProvider() != null) {
                        lore.add(CC.GREEN + "Left click to teleport.");
                    }
                    lore.add(CC.GREEN + "Right click to remove.");

                    paginate(index.getAndIncrement(),
                            ItemBuilder.getPlayerHead(lastReport.getTarget().getRealName())
                                    .setAmount(reports.size())
                                    .setName((lastReport.isOnline() ? CC.BOLD_GREEN : CC.BOLD_RED) + lastReport.getTarget().getColoredDisplayName())
                                    .setLore(lore)
                                    .build()).onClick(clickType -> {
                        if (clickType.isLeftClick()) {
                            if (reportService.getReportProvider() != null && lastReport.isOnline()) {
                                reportService.getReportProvider().teleport(user, lastReport.getTarget());
                            }
                        }
                        if (clickType.isRightClick()) {
                            new ConfirmMenu(user, false, check -> {
                                if (check) reportService.removeReports(lastReport.getTarget().getUniqueId());

                                new ReportViewMenu(user, reportService).open();
                            }).open();
                        }
                    });
                });
    }
}