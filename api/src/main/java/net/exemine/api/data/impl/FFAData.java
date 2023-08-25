package net.exemine.api.data.impl;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.ExeData;
import net.exemine.api.data.stat.number.impl.IntTimedStat;
import net.exemine.api.database.DatabaseCollection;

@Getter
@Setter
public class FFAData extends ExeData {

    private final IntTimedStat elo = new IntTimedStat(1400);
    private final IntTimedStat kills = new IntTimedStat();
    private final IntTimedStat deaths = new IntTimedStat();
    private final IntTimedStat highestStreak = new IntTimedStat();
    private final IntTimedStat swordHits = new IntTimedStat();
    private final IntTimedStat landedSwordHits = new IntTimedStat();
    private final IntTimedStat arrowShots = new IntTimedStat();
    private final IntTimedStat landedArrowShots = new IntTimedStat();

    private int currentStreak;

    private StaffData staffData = new StaffData();

    private boolean deathMessages = true;

    private String kitLayout;

    public boolean hasNotSetupKitLayout() {
        return kitLayout == null;
    }

    @Override
    public DatabaseCollection getMongoCollection() {
        return DatabaseCollection.USERS_FFA;
    }

    @Getter
    @Setter
    public static class StaffData {

        private boolean showGameModerators = true;
        private boolean edit = false;
    }
}
