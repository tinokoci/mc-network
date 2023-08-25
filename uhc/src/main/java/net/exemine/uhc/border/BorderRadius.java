package net.exemine.uhc.border;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BorderRadius {

    RADIUS_2000(2000),
    RADIUS_1500(1500),
    RADIUS_1000(1000),
    RADIUS_500(500),
    RADIUS_100(100),
    RADIUS_50(50),
    RADIUS_25(25);

    private final int value;

    public boolean isEqual(BorderRadius radius) {
        return this == radius;
    }

    public boolean isEqualOrHigher(BorderRadius radius) {
        return isEqual(radius) || ordinal() < radius.ordinal();
    }

    public boolean isEqualOrLower(BorderRadius radius) {
        return isEqual(radius) || ordinal() > radius.ordinal();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
