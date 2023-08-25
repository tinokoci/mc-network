package net.exemine.core.menu.pagination;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@RequiredArgsConstructor
@Getter
public enum PaginationItem {

    LEVER(Material.LEVER, -1),
    CARPET(Material.CARPET, 5);

    private final Material material;
    private final int durability;
}
