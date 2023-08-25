package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.report.ReportService;
import net.exemine.core.report.menu.ReportMenu;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ReportCommand extends BaseCommand<CoreUser, CoreData> {

    private final ReportService reportService;

    public ReportCommand(ReportService reportService) {
        super(List.of("report"));
        this.reportService = reportService;
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (!reportService.canReport(user)) return;

        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /report <player>");
            return;
        }
        CoreUser target = userService.get(args[0]);
        if (isUserOffline(user, target)) return;

        if (user == target) {
            user.sendMessage(CC.RED + "You cannot report yourself.");
            return;
        }
        new ReportMenu(user, target, reportService).open();
    }
}