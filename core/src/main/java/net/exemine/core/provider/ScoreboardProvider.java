package net.exemine.core.provider;

import net.exemine.core.provider.scoreboard.PlayerScoreboard;
import net.exemine.core.user.base.ExeUser;
import org.bukkit.scoreboard.Scoreboard;

public interface ScoreboardProvider<T extends ExeUser<?>> {

    String getTitle();

    void update(T user, PlayerScoreboard<T> board);

    default void loadObjectives(T user, Scoreboard scoreboard) {
        // do nothing
    }

    default void beforeUpdate() {
        // do nothing
    }
}
