package net.exemine.uhc.user.info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

@RequiredArgsConstructor
@Getter
public class RelogInfo {

    private final Location location;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final float fallDistance;
    private final double health;
    private final int foodLevel;
    private final float saturation;
    private final float exp;
    private final int level;
    private final int fireTicks;
    private final Collection<PotionEffect> activePotionEffects;
}
