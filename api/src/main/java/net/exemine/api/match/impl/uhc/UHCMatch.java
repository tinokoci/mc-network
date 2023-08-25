package net.exemine.api.match.impl.uhc;

import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.Setter;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.match.Match;
import net.exemine.api.model.ScenarioName;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class UHCMatch extends Match {

    private long startTime;
    private String approvalMessageId;
    private UUID hostUuid;
    private UUID supervisorUuid;
    private String mode;
    private Set<ScenarioName> scenarios;
    private Set<UUID> participants;
    private Set<UUID> winnerUuids;
    private boolean nether;
    private boolean spectating;
    private boolean firstAlert;
    private boolean secondAlert;
    private boolean summarySent;
    private int thanksCount;
    private int initialPlayerCount;
    private int initialBorder;

    private final RunningInfo runningInfo = new RunningInfo();

    public boolean hasSupervisor() {
        return supervisorUuid != null;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > startTime;
    }

    public boolean isTeamGame() {
        return !mode.equalsIgnoreCase("FFA");
    }

    public void setSecondAlert(boolean secondAlert) {
        this.secondAlert = secondAlert;
        setFirstAlert(true);
    }

    @Nullable
    public String getApprovalMessageId() {
        return approvalMessageId;
    }

    @Override
    public void init() {
        super.init();
        mode = "Unknown";
        scenarios = new LinkedHashSet<>();
        participants = new LinkedHashSet<>();
        winnerUuids = new LinkedHashSet<>();
    }

    @Override
    protected DatabaseCollection getMongoCollection() {
        return DatabaseCollection.MATCHES_UHC;
    }

    @Setter
    @Getter
    public static class RunningInfo {

        private String gameState = "Unknown";
        private Set<String> winnerNames = new LinkedHashSet<>();
        private int alivePlayerCount;
        private int aliveTeamCount;
        private int spectatorCount;
        private int currentBorder;
        private int winningTeamKills;
        private int scatteredCount;
        private int scatteringCount;
        private boolean whitelisted;
    }
}
