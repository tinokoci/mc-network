package net.exemine.uhc.practice.layout;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.stream.IntStream;

public class PracticeLayoutSetupMenu extends Menu<UHCUser> {

    private static final int LAYOUT_SIZE = 9 * 4;
    private boolean initialRender = true;

    public PracticeLayoutSetupMenu(UHCUser user) {
        super(user, CC.DARK_GRAY + "Setup the inventory:", 6);
        setAllowItemsMovement(true);
        setItemMovementBound(new int[]{0, LAYOUT_SIZE});
    }

    @Override
    public void update() {
        resetItems(!initialRender);
        if (initialRender) initialRender = false;

        IntStream.range(36, 45).forEach(index -> set(index, getPlaceholderItem()));
        IntStream.of(45, 46, 47, 51, 52, 53).forEach(index -> set(index, new ItemBuilder()
                .setMaterial(Material.STAINED_GLASS_PANE)
                .setDurability(7)
                .setName(" ")
                .build()));
        set(48, new ItemBuilder()
                .setMaterial(Material.STAINED_CLAY)
                .setName(CC.BOLD_RED + "Cancel Setup")
                .setDurability(14)
                .build()
        ).onClick(() -> {
            user.playFailure();
            close(false);
        });
        set(49, new ItemBuilder()
                .setMaterial(Material.CLAY_BALL)
                .setName(CC.YELLOW + "Reset To Default")
                .build()
        ).onClick(() -> {
            open();
            user.playClick();
        });
        set(50, new ItemBuilder()
                .setMaterial(Material.STAINED_CLAY)
                .setName(CC.BOLD_GREEN + "Save Inventory")
                .setDurability(13)
                .build()
        ).onClick(() -> {
            ItemStack[] inventoryLayout = new ItemStack[LAYOUT_SIZE];

            IntStream.range(0, LAYOUT_SIZE).forEach(i -> {
                ItemStack item = getInventory().getItem(i);
                if (item == null) return;
                int slot = i < 9 * 3 ? i + 9 : i - 9 * 3; // shift 4th row back to the hotbar row
                inventoryLayout[slot] = new ItemStack(item.getType(), item.getAmount()); // Has to be cloned because of how menus work
            });
            user.getData().setPracticeLayout(ItemUtil.serializeItemArray(inventoryLayout));
            user.saveData(true);
            user.playSuccess();

            close(false);
            user.performCommand("practice");
        });
    }

    private void resetItems(boolean forceDefaultLayout) {
        UHCData data = user.getData();
        ItemStack[] deserializedUserLayout = ItemUtil.deserializeItemArray(data.getPracticeLayout());
        ItemStack[] layout = forceDefaultLayout || data.hasNotSetupPracticeLayout() || deserializedUserLayout == null
                ? PracticeLayout.get()
                : deserializedUserLayout;

        IntStream.range(0, LAYOUT_SIZE).forEach(i -> {
            int slot = i < 9 ? i + 9 * 3 : i - 9; // shift hotbar row to the 4th row in the menu
            ItemStack item = layout[i];
            if (item == null) return;
            set(slot, item);
        });
    }
}
