package net.exemine.core.cosmetic.color;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.string.CC;
import net.exemine.core.cosmetic.CosmeticMenu;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ColorSelectMenu extends PaginatedMenu<CoreUser> {

    private final CoreData.CosmeticData cosmeticData;

    public ColorSelectMenu(CoreUser user) {
        super(user, CC.DARK_GRAY + "Color Selector", 4, 2);
        this.cosmeticData = user.getCosmeticData();
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addReturnItem(new CosmeticMenu(user));
        addExitItem();

        if (user.getColorType() != null) {
            set(getSize() - 5, new ItemBuilder()
                    .setMaterial(Material.SULPHUR)
                    .setName(CC.RED + "Remove Color")
                    .setLore(CC.GRAY + "Click here to remove the selected color.")
                    .build()
            ).onClick(() -> {
                cosmeticData.setColorType(null);
                user.saveData(true);
                user.sendMessage(CC.PURPLE + "[Cosmetic] " + CC.GRAY + "You have removed your color.");
            });
        }
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger(0);

        Arrays.stream(ColorType.values()).forEach(colorType -> {
            boolean selected = cosmeticData.getColorType() != null && cosmeticData.getColorType().equals(colorType.getName());
            boolean unlocked = user.hasUnlockedCosmetic(colorType);

            paginate(index.getAndIncrement(), new ItemBuilder()
                    .setMaterial(Material.WOOL)
                    .setDurability(ItemUtil.getWoolData(colorType.getFormat()))
                    .setName((selected ? CC.BOLD_GREEN : unlocked ? CC.BOLD_PINK : CC.BOLD_RED) + colorType.getName())
                    .setLore(lore -> {
                        lore.add("");
                        lore.add(CC.GRAY + "Format: ");
                        lore.add(user.getFormattedTag() + user.getRank().getPrefix() + colorType.getFormat() + user.getRealName() + CC.GRAY + ": " + CC.WHITE + "hi");
                        lore.add("");
                        lore.add(selected ? CC.GREEN + "You have selected this color."
                                : unlocked ? CC.GREEN + "Click to select this color."
                                : CC.RED + "You don't own this color.");
                    })
                    .build()
            ).onClick(() -> {
                if (selected || !unlocked) return;

                cosmeticData.setColorType(colorType.getName());
                user.saveData(true);
                user.sendMessage(CC.PURPLE + "[Cosmetic] " + CC.GRAY + "You have selected the " + CC.GOLD + colorType.getName() + CC.GRAY + " color.");
            });
        });
    }
}