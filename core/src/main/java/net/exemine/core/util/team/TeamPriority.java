package net.exemine.core.util.team;

public enum TeamPriority {

    TABLIST,
    NAMETAG,
    NPC;

    @Override
    public String toString() {
        return String.valueOf(ordinal());
    }
}
