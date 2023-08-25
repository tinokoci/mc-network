package net.exemine.uhc.spectator.tracker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PlayerTrackerType {

    ALL("All"),
    MINING("Mining"),
    NETHER("Nether"),
    PRACTICE("Practice");

    private final String name;
}
