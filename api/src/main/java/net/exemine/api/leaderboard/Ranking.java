package net.exemine.api.leaderboard;

import java.util.UUID;

public interface Ranking {

    UUID getUniqueId();

    String getName();

    String getDisplayName();

    String getFormattedValue();

    int getPlacing();
}
