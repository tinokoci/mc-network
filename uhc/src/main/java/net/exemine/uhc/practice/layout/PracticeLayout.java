package net.exemine.uhc.practice.layout;

import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PracticeLayout {

    private static final ItemStack[] DEFAULT_LAYOUT = new ItemStack[9 * 4];

    static {
        DEFAULT_LAYOUT[0] = new ItemBuilder(Material.DIAMOND_SWORD).unbreakable().build();
        DEFAULT_LAYOUT[1] = new ItemBuilder(Material.BOW).unbreakable().build();
        DEFAULT_LAYOUT[2] = new ItemBuilder(Material.FISHING_ROD).unbreakable().build();
        DEFAULT_LAYOUT[9] = new ItemStack(Material.ARROW, 8);
    }

    public static ItemStack[] get() {
        return DEFAULT_LAYOUT;
    }
}
