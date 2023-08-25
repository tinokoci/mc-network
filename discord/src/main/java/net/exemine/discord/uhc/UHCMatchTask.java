package net.exemine.discord.uhc;

import net.exemine.api.data.DataService;
import net.exemine.api.match.MatchService;
import net.exemine.api.match.MatchState;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.properties.PropertiesService;
import net.exemine.api.redis.RedisService;
import net.exemine.api.twitter.TwitterService;
import net.exemine.api.util.Executor;
import net.exemine.api.util.TimeUtil;
import net.exemine.discord.uhc.hook.AutoGameSetupHook;
import net.exemine.discord.uhc.hook.MatchAlertsHook;
import net.exemine.discord.uhc.hook.UpcomingGamesHook;
import net.exemine.discord.uhc.hook.WinnerHook;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UHCMatchTask {

    private final MatchService matchService;

    private final AutoGameSetupHook autoGameSetupHook;
    private final MatchAlertsHook matchAlertsHook;
    private final UpcomingGamesHook upcomingGamesHook;
    private final WinnerHook winnerHook;

    public UHCMatchTask(MatchService matchService,
                        RedisService redisService,
                        PropertiesService propertiesService,
                        DataService dataService,
                        TwitterService twitterService) {
        this.matchService = matchService;
        this.autoGameSetupHook = new AutoGameSetupHook(redisService);
        this.matchAlertsHook = new MatchAlertsHook(matchService, dataService, twitterService);
        this.upcomingGamesHook = new UpcomingGamesHook(propertiesService, dataService);
        this.winnerHook = new WinnerHook(matchService, dataService);
        Executor.schedule(this::run).runAsyncTimer(0, 10_000L);
    }

    private void run() {
        List<UHCMatch> waitingMatches = matchService.getAllMatches(UHCMatch.class)
                .stream()
                .filter(this::isMatchWaitingNotExpired)
                .sorted(Comparator.comparingLong(UHCMatch::getStartTime))
                .collect(Collectors.toList());

        upcomingGamesHook.run(waitingMatches);
        matchAlertsHook.run(waitingMatches
                .stream()
                .findFirst()
                .orElse(null));
        autoGameSetupHook.run(waitingMatches
                .stream()
                .filter(this::isMatchSetupReady)
                .findFirst()
                .orElse(null));
        winnerHook.run(matchService.getAllMatches(UHCMatch.class)
                .stream()
                .filter(this::isMatchSummaryReady)
                .findFirst()
                .orElse(null));
    }

    private boolean isMatchWaitingNotExpired(UHCMatch match) {
        return match.getState() == MatchState.WAITING && !match.isExpired();
    }

    private boolean isMatchSetupReady(UHCMatch match) {
        return match.getStartTime() - System.currentTimeMillis() <= TimeUtil.MINUTE * 10;
    }

    private boolean isMatchSummaryReady(UHCMatch match) {
        return match.isCompleted() && !match.isSummarySent() && match.getWinnerUuids() != null &&
            Instant.ofEpochMilli(match.getStartTime()).isAfter(Instant.ofEpochMilli(1680537844066L));
        // This is hack to prevent old games from being posted in the winners channel
    }
}