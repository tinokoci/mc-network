package net.exemine.core.report.menu;

import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.report.ReportService;
import net.exemine.core.report.type.ReportType;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;
import net.exemine.core.util.item.ItemBuilder;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportMenu extends Menu<CoreUser> {

    private final ReportService reportService;

    public ReportMenu(CoreUser user, CoreUser target, ReportService reportService) {
        super(user, CC.DARK_GRAY + "Select a reason:", ReportType.getSize(InstanceUtil.getType()));
        this.reportService = reportService;
        setTarget(target);
    }

    @Override
    public void update() {
        AtomicInteger index = new AtomicInteger();

        Arrays.stream(ReportType.values())
                .filter(report -> report.getType() == null || report.getType() == InstanceUtil.getType())
                .forEach(type -> set(index.getAndIncrement(), new ItemBuilder(type.getMaterial())
                        .setName(CC.PINK + type.getName())
                        .build()
                ).onClick(() -> {
                    reportService.addReport(user, target, type);
                    close(false);
                }));
    }
}

