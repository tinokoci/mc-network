package net.exemine.core.cosmetic.rod;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.string.CC;
import net.exemine.core.cosmetic.CosmeticMenu;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class RodTrailSelectMenu extends PaginatedMenu<CoreUser> {

    private final CoreData.CosmeticData cosmeticData;

    public RodTrailSelectMenu(CoreUser user) {
        super(user, CC.DARK_GRAY + "Rod Trail Selector", 4, 2);
        this.cosmeticData = user.getCosmeticData();
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addReturnItem(new CosmeticMenu(user));
        addExitItem();

        if (user.getRodTrail() != null) {
            set(getSize() - 5, new ItemBuilder()
                    .setMaterial(Material.SULPHUR)
                    .setName(CC.RED + "Remove Rod Trail")
                    .setLore(CC.GRAY + "Click here to remove the selected rod trail.")
                    .build()
            ).onClick(() -> {
                cosmeticData.setRodTrail(null);
                user.saveData(true);
                user.sendMessage(CC.PURPLE + "[Cosmetic] " + CC.GRAY + "You have removed your rod trail.");
            });
        }
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        Arrays.stream(RodTrail.values()).forEach(rodTrail -> {
            boolean selected = cosmeticData.getRodTrail() != null && cosmeticData.getRodTrail().equals(rodTrail.getName());
            boolean unlocked = user.hasUnlockedCosmetic(rodTrail);

            paginate(index.getAndIncrement(), new ItemBuilder()
                    .setMaterial(rodTrail.getDisplayItem())
                    .setName((selected ? CC.BOLD_GREEN : unlocked ? CC.BOLD_PINK : CC.BOLD_RED) + rodTrail.getName())
                    .setLore(lore -> {
                        lore.add("");
                        lore.add(CC.GRAY + "Change your rod trail");
                        lore.add(CC.GRAY + "to " + CC.WHITE + rodTrail.getName() + CC.GRAY + '.');
                        lore.add("");
                        lore.add(selected ? CC.GREEN + "You have selected this rod trail."
                                : unlocked ? CC.GREEN + "Click to select this rod trail."
                                : CC.RED + "You don't own this rod trail.");
                    })
                    .build()
            ).onClick(() -> {
                if (selected || !unlocked) return;

                cosmeticData.setRodTrail(rodTrail.getName());
                user.saveData(true);
                user.sendMessage(CC.PURPLE + "[Cosmetic] " + CC.GRAY + "You have selected the " + CC.GOLD + rodTrail.getName() + CC.GRAY + " rod trail.");
            });
        });
    }
}
