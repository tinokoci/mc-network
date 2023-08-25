package net.exemine.core.menu.pagination;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaginationType {

    NINE(9),
    SEVEN(7);

    private final int itemsPerRow;
}
