package net.exemine.core.cosmetic.tag;

import net.exemine.api.cosmetic.tag.TagService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.string.CC;
import net.exemine.core.cosmetic.CosmeticMenu;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import org.bukkit.Material;

import java.util.concurrent.atomic.AtomicInteger;

public class TagSelectMenu extends PaginatedMenu<CoreUser> {

    private final TagService tagService;
    private final CoreData.CosmeticData cosmeticData;

    public TagSelectMenu(CoreUser user) {
        super(user, CC.DARK_GRAY + "Tag Selector", 4, 2);
        this.tagService = user.getPlugin().getTagService();
        this.cosmeticData = user.getCosmeticData();
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addReturnItem(new CosmeticMenu(user));
        addExitItem();

        if (user.getTag() != null) {
            set(getSize() - 5, new ItemBuilder()
                    .setMaterial(Material.SULPHUR)
                    .setName(CC.RED + "Remove Tag")
                    .setLore(CC.GRAY + "Click here to remove the selected tag.")
                    .build()
            ).onClick(() -> {
                cosmeticData.setTag(null);
                user.saveData(true);
                user.sendMessage(CC.PURPLE + "[Cosmetic] " + CC.GRAY + "You have removed your tag.");
            });
        }
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        tagService.getAllTags().forEach(tag -> {
            boolean selected = cosmeticData.getTag() != null && cosmeticData.getTag().equals(tag.getName());
            boolean unlocked = user.hasUnlockedCosmetic(tag);

            paginate(index.getAndIncrement(), new ItemBuilder(Material.WOOL)
                    .setDurability(selected ? ItemUtil.getGreen() : unlocked ? ItemUtil.getYellow() : ItemUtil.getRed())
                    .setName((selected ? CC.BOLD_GREEN : unlocked ? CC.BOLD_PINK : CC.BOLD_RED) + tag.getName())
                    .setLore(lore -> {
                        lore.add("");
                        lore.add(CC.GRAY + "Format: ");
                        lore.add(CC.translate(tag.getFormat()) + ' ' + user.getRank().getPrefix() + user.getFormattedColorType() + user.getRealName() + CC.GRAY + ": " + CC.WHITE + "hi");
                        lore.add("");
                        lore.add(selected ? CC.GREEN + "You have selected this tag."
                                : unlocked ? CC.GREEN + "Click to select this tag."
                                : CC.RED + "You don't own this tag.");
                    })
                    .build()
            ).onClick(() -> {
                if (selected || !unlocked) return;

                cosmeticData.setTag(tag.getName());
                user.saveData(true);
                user.sendMessage(CC.PURPLE + "[Cosmetic] " + CC.GRAY + "You have selected the " + CC.WHITE + CC.translate(tag.getFormat()) + CC.GRAY + " tag.");
            });
        });
    }
}
