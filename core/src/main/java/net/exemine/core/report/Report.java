package net.exemine.core.report;

import lombok.Getter;
import net.exemine.core.report.type.ReportType;
import net.exemine.core.user.CoreUser;

@Getter
public class Report {

    private final String issuer;
    private final ReportType type;
    private final CoreUser target;
    private final long addedAt;

    public Report(String issuer, CoreUser target, ReportType type) {
        this.issuer = issuer;
        this.target = target;
        this.type = type;
        this.addedAt = System.currentTimeMillis();
    }

    public boolean isOnline() {
        return target != null && target.isOnline();
    }

    public boolean match(Report report) {
        return issuer.equals(report.getIssuer())
                && type == report.getType()
                && target == report.getTarget();
    }
}
