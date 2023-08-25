package net.exemine.uhc.user.info;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.exemine.api.data.stat.number.impl.SimpleIntStat;
import net.exemine.uhc.user.UHCUser;

import java.util.LinkedHashMap;
import java.util.UUID;

@Getter
@Setter
@ToString
public class GameInfo {

    private final SimpleIntStat eloGained = new SimpleIntStat();
    private final SimpleIntStat kills = new SimpleIntStat();
    private final SimpleIntStat swordHits = new SimpleIntStat();
    private final SimpleIntStat landedSwordHits = new SimpleIntStat();
    private final SimpleIntStat arrowShots = new SimpleIntStat();
    private final SimpleIntStat landedArrowShots = new SimpleIntStat();

    private final SimpleIntStat minedDiamonds = new SimpleIntStat();
    private final SimpleIntStat minedGold = new SimpleIntStat();
    private final SimpleIntStat minedIron = new SimpleIntStat();
    private final SimpleIntStat minedRedstone = new SimpleIntStat();
    private final SimpleIntStat minedLapis = new SimpleIntStat();
    private final SimpleIntStat minedCoal = new SimpleIntStat();
    private final SimpleIntStat minedQuartz = new SimpleIntStat();

    private final SimpleIntStat levelsEarned = new SimpleIntStat();
    private final SimpleIntStat nethersEntered = new SimpleIntStat();

    private final SimpleIntStat practiceKills = new SimpleIntStat();
    private final SimpleIntStat practiceDeaths = new SimpleIntStat();
    private final SimpleIntStat practiceStreak = new SimpleIntStat();
    
    private LinkedHashMap<Long, UUID> killedUsers = new LinkedHashMap<>();

    private boolean died;
    private boolean played;
    private boolean killedByBannedUser;

    private boolean winner;
    private boolean diedInTop5;
    private boolean carriedToVictory;

    private UUID loggerKillerUuid;

    public void addKilledUser(UHCUser user) {
        killedUsers.put(System.currentTimeMillis(), user.getUniqueId());
    }
}
