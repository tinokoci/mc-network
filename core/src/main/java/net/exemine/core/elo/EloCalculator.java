package net.exemine.core.elo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class EloCalculator {

    public static int getEloChange(int killerElo, int victimElo, Result result) {
        return getEloChange(killerElo, victimElo, 32, result);
    }

    public static int getEloChange(int killerElo, int victimElo, double kFactor, Result result) {
        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, ((victimElo - killerElo) / 400.0)));
        return (int) (kFactor * (result.getValue() - expectedScore));
    }

    @Getter
    @RequiredArgsConstructor
    public enum Result {
        WIN(1),
        LOSS(0);

        private final int value;
    }
}
