package net.exemine.api.data.impl;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.ExeData;
import net.exemine.api.data.stat.number.impl.IntTimedStat;
import net.exemine.api.data.stat.string.StringTimedStat;
import net.exemine.api.database.DatabaseCollection;

@Getter
@Setter
public class UHCData extends ExeData {

    private final IntTimedStat elo = new IntTimedStat(1400);
    private final IntTimedStat wins = new IntTimedStat();
    private final IntTimedStat kills = new IntTimedStat();
    private final IntTimedStat deaths = new IntTimedStat();
    private final IntTimedStat swordHits = new IntTimedStat();
    private final IntTimedStat landedSwordHits = new IntTimedStat();
    private final IntTimedStat arrowShots = new IntTimedStat();
    private final IntTimedStat landedArrowShots = new IntTimedStat();

    private final IntTimedStat minedDiamonds = new IntTimedStat();
    private final IntTimedStat minedGold = new IntTimedStat();
    private final IntTimedStat minedIron = new IntTimedStat();
    private final IntTimedStat minedRedstone = new IntTimedStat();
    private final IntTimedStat minedLapis = new IntTimedStat();
    private final IntTimedStat minedCoal = new IntTimedStat();
    private final IntTimedStat minedQuartz = new IntTimedStat();

    private final IntTimedStat gamesPlayed = new IntTimedStat();
    private final IntTimedStat top5s = new IntTimedStat();
    private final IntTimedStat carriedWins = new IntTimedStat();
    private final IntTimedStat levelsEarned = new IntTimedStat();
    private final IntTimedStat nethersEntered = new IntTimedStat();

    private final StringTimedStat itemContents = new StringTimedStat();
    private final StringTimedStat armorContents = new StringTimedStat();

    private StaffData staffData = new StaffData();

    private boolean showSpectators = true;
    private boolean deathMessages = true;

    private String practiceLayout;

    public boolean hasNotSetupPracticeLayout() {
        return practiceLayout == null;
    }

    @Override
    public DatabaseCollection getMongoCollection() {
        return DatabaseCollection.USERS_UHC;
    }

    @Getter
    @Setter
    public static class StaffData {

        private boolean xrayAlerts = true;
        private boolean helpOpAlerts = true;
        private boolean hostChatMessages = true;
        private boolean spectatorChatMessages = true;
        private boolean showGameModerators = true;
    }
}
