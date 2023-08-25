package net.exemine.uhc.spectator.menu;

import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.stream.IntStream;

public class ChestViewerMenu extends Menu<UHCUser> {

    private final int size;
    private final ItemStack[] contents;

    public ChestViewerMenu(UHCUser user, int size, ItemStack[] contents) {
        super(user, CC.DARK_GRAY + "Chest Inspector", size / 9);
        this.size = size;
        this.contents = contents;
    }

    @Override
    public void update() {
        IntStream.range(0, size).forEach(i -> {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) return;
            set(i, item);
        });
    }
}
