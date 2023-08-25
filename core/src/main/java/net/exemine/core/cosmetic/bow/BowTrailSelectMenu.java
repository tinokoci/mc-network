package net.exemine.core.cosmetic.bow;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.string.CC;
import net.exemine.core.cosmetic.CosmeticMenu;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class BowTrailSelectMenu extends PaginatedMenu<CoreUser> {

    private final CoreData.CosmeticData cosmeticData;

    public BowTrailSelectMenu(CoreUser user) {
        super(user, CC.DARK_GRAY + "Bow Trail Selector", 4, 2);
        this.cosmeticData = user.getCosmeticData();
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addReturnItem(new CosmeticMenu(user));
        addExitItem();

        if (user.getBowTrail() != null) {
            set(getSize() - 5, new ItemBuilder()
                    .setMaterial(Material.SULPHUR)
                    .setName(CC.RED + "Remove Bow Trail")
                    .setLore(CC.GRAY + "Click here to remove the selected bow trail.")
                    .build()
            ).onClick(() -> {
                cosmeticData.setBowTrail(null);
                user.saveData(true);
                user.sendMessage(CC.PURPLE + "[Cosmetic] " + CC.GRAY + "You have removed your bow trail.");
            });
        }
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        Arrays.stream(BowTrail.values()).forEach(bowTrail -> {
            boolean selected = cosmeticData.getBowTrail() != null && cosmeticData.getBowTrail().equals(bowTrail.getName());
            boolean unlocked = user.hasUnlockedCosmetic(bowTrail);

            paginate(index.getAndIncrement(), new ItemBuilder()
                    .setMaterial(bowTrail.getDisplayItem())
                    .setName((selected ? CC.BOLD_GREEN : unlocked ? CC.BOLD_PINK : CC.BOLD_RED) + bowTrail.getName())
                    .setLore(lore -> {
                        lore.add("");
                        lore.add(CC.GRAY + "Change your bow trail");
                        lore.add(CC.GRAY + "to " + CC.WHITE + bowTrail.getName() + CC.GRAY + '.');
                        lore.add("");
                        lore.add(selected ? CC.GREEN + "You have selected this bow trail."
                                : unlocked ? CC.GREEN + "Click to select this bow trail."
                                : CC.RED + "You don't own this bow trail.");
                    })
                    .build()
            ).onClick(() -> {
                if (selected || !unlocked) return;

                cosmeticData.setBowTrail(bowTrail.getName());
                user.saveData(true);
                user.sendMessage(CC.PURPLE + "[Cosmetic] " + CC.GRAY + "You have selected the " + CC.GOLD + bowTrail.getName() + CC.GRAY + " bow trail.");
            });
        });
    }
}
