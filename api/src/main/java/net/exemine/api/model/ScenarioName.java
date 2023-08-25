package net.exemine.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ScenarioName {

    CUT_CLEAN("CutClean"),
    TIME_BOMB("Time Bomb"),
    TIMBER("Timber"),
    INSTANT_INV("Instant Inv"),
    FIRE_LESS("Fireless"),
    BOW_LESS("Bowless"),
    ROD_LESS("Rodless"),
    NO_FALL("No Fall"),
    HASTEY_BOYS("HasteyBoys"),
    NO_CLEAN("No Clean"),
    BACKPACKS("Backpacks"),
    TRIPLE_ORES("Triple Ores"),
    DOUBLE_ORES("Double Ores"),
    OP_HASTEY_BOYS("OP HasteyBoys"),
    HORSE_LESS("Horseless"),
    LUCKY_LEAVES("Lucky Leaves"),
    GOLD_LESS("Goldless"),
    DIAMOND_LESS("Diamondless"),
    ORE_FRENZY("Ore Frenzy"),
    SOUP("Soup"),
    APRIL_FOOLS("April Fools"),
    FLOWER_POWER("Flower Power"),
    VANILLA_PLUS("Vanilla+"),
    BARE_BONES("Bare Bones"),
    LIMITATIONS("Limitations"),
    BLOOD_DIAMONDS("Blood Diamonds"),
    BLOOD_ENCHANTS("Blood Enchants"),
    VEIN_MINER("Vein Miner"),
    COLD_WEAPONS("Cold Weapons"),
    SAFE_LOOT("Safe Loot"),
    NO_CLEAN_PLUS("No Clean+"),
    ANONYMOUS("Anonymous"),
    LIMITED_ENCHANTS("Limited Enchants"),
    BOUNTY_HUNTER("Bounty Hunter"),
    DO_NOT_DISTURB("Do Not Disturb"),
    BLEEDING_SWEETS("Bleeding Sweets"),
    GOLDEN_RETRIEVER("Golden Retriever"),
    WEB_CAGE("Web Cage"),
    BROADCASTER("Broadcaster"),
    RADAR("Radar");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public static ScenarioName get(String name) {
        return Arrays.stream(values())
                .filter(scenario -> name.replace(" ", "").equalsIgnoreCase(scenario.getName().replace(" ", "")))
                .findFirst()
                .orElse(null);
    }
}
