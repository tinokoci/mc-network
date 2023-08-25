package net.exemine.uhc.user.info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
@Getter
public class RespawnInfo {

    private final Location respawnLocation;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final float exp;
    private final int level;
}
