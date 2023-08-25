package net.exemine.api.rank;

public enum RankType {

    STAFF,
    CONTENT_CREATOR,
    DONATOR,
    DEFAULT;

    public static RankType get(String name) {
        try {
            return RankType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
