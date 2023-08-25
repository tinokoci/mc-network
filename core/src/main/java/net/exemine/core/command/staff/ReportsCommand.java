package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.report.ReportService;
import net.exemine.core.report.menu.ReportViewMenu;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ReportsCommand extends BaseCommand<CoreUser, CoreData> {

    private final ReportService reportService;

    public ReportsCommand(ReportService reportService) {
        super(List.of("reports", "reportview", "viewreports", "seereports", "reportlist"), Rank.TRIAL_MOD);
        this.reportService = reportService;
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        new ReportViewMenu(user, reportService).open();
    }
}
