package net.exemine.core.cosmetic;

import net.exemine.api.cosmetic.tag.Tag;
import net.exemine.api.cosmetic.tag.TagService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.cosmetic.bow.BowTrail;
import net.exemine.core.cosmetic.bow.BowTrailSelectMenu;
import net.exemine.core.cosmetic.color.ColorSelectMenu;
import net.exemine.core.cosmetic.color.ColorType;
import net.exemine.core.cosmetic.rod.RodTrail;
import net.exemine.core.cosmetic.rod.RodTrailSelectMenu;
import net.exemine.core.cosmetic.tag.TagSelectMenu;
import net.exemine.core.menu.Menu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CosmeticMenu extends Menu<CoreUser> {

    private final TagService tagService;
    private final CoreData.CosmeticData cosmeticData;

    public CosmeticMenu(CoreUser user) {
        super(user, CC.DARK_GRAY + "Cosmetics", 3);
        this.tagService = user.getPlugin().getTagService();
        this.cosmeticData = user.getCosmeticData();
        setAutoSurround(true);
    }

    @Override
    public void update() {
        addExitItem();

        set(10, new ItemBuilder()
                .setMaterial(Material.WOOL)
                .setDurability(user.getColorType() == null
                        ? -1
                        : ItemUtil.getWoolData(user.getColorType().getFormat()))
                .setName(CC.PINK + "Colors")
                .setLore(() -> {
                    List<String> lore = new ArrayList<>();
                    lore.add("");
                    lore.add(CC.GRAY + "Don't want to blend in with others?");
                    lore.add(CC.GRAY + "Select a color to style your name.");
                    lore.add("");

                    List<ColorType> colorTypes = List.of(ColorType.values());
                    int total = colorTypes.size();
                    int unlocked = (int) colorTypes.stream().filter(user::hasUnlockedCosmetic).count();
                    String percentage = StringUtil.formatNumber((int) ((float) unlocked / total * 100));
                    String selected = Optional.ofNullable(cosmeticData.getColorType()).orElse("None");

                    lore.add(CC.GRAY + "Unlocked: " + CC.WHITE + unlocked + '/' + total + CC.DARK_GRAY + " (" + percentage + "%)");
                    lore.add(CC.GRAY + "Selected: " + CC.WHITE + selected);
                    lore.add("");
                    lore.add(CC.GREEN + "Click to browse.");

                    return lore;
                })
                .build()
        ).onClick(() -> new ColorSelectMenu(user).open());

        set(12, new ItemBuilder()
                .setMaterial(Material.BOW)
                .setName(CC.PINK + "Bow Trails")
                .setLore(() -> {
                    List<String> lore = new ArrayList<>();
                    lore.add("");
                    lore.add(CC.GRAY + "Particle effects that appear");
                    lore.add(CC.GRAY + "when you shoot an arrow.");
                    lore.add("");

                    List<BowTrail> bowTrails = List.of(BowTrail.values());
                    int total = bowTrails.size();
                    int unlocked = (int) bowTrails.stream().filter(user::hasUnlockedCosmetic).count();
                    String percentage = StringUtil.formatNumber((int) ((float) unlocked / total * 100));
                    String selected = Optional.ofNullable(cosmeticData.getBowTrail()).orElse("None");

                    lore.add(CC.GRAY + "Unlocked: " + CC.WHITE + unlocked + '/' + total + CC.DARK_GRAY + " (" + percentage + "%)");
                    lore.add(CC.GRAY + "Selected: " + CC.WHITE + selected);
                    lore.add("");
                    lore.add(CC.GREEN + "Click to browse.");

                    return lore;
                })
                .build()
        ).onClick(() -> new BowTrailSelectMenu(user).open());

        set(14, new ItemBuilder()
                .setMaterial(Material.FISHING_ROD)
                .setName(CC.PINK + "Rod Trails")
                .setLore(() -> {
                    List<String> lore = new ArrayList<>();
                    lore.add("");
                    lore.add(CC.GRAY + "Particle effects that appear");
                    lore.add(CC.GRAY + "when you use a fishing rod.");
                    lore.add("");

                    List<RodTrail> rodTrails = List.of(RodTrail.values());
                    int total = rodTrails.size();
                    int unlocked = (int) rodTrails.stream().filter(user::hasUnlockedCosmetic).count();
                    String percentage = StringUtil.formatNumber((int) ((float) unlocked / total * 100));
                    String selected = Optional.ofNullable(cosmeticData.getRodTrail()).orElse("None");

                    lore.add(CC.GRAY + "Unlocked: " + CC.WHITE + unlocked + '/' + total + CC.DARK_GRAY + " (" + percentage + "%)");
                    lore.add(CC.GRAY + "Selected: " + CC.WHITE + selected);
                    lore.add("");
                    lore.add(CC.GREEN + "Click to browse.");

                    return lore;
                })
                .build()
        ).onClick(() -> new RodTrailSelectMenu(user).open());

        set(16, new ItemBuilder()
                .setMaterial(Material.NAME_TAG)
                .setName(CC.PINK + "Tags")
                .setLore(() -> {
                    List<String> lore = new ArrayList<>();
                    lore.add("");
                    lore.add(CC.GRAY + "Special tags next to your");
                    lore.add(CC.GRAY + "name to make you stand out.");
                    lore.add("");

                    List<Tag> tags = tagService.getAllTags();
                    int total = tags.size();
                    int unlocked = (int) tags.stream().filter(user::hasUnlockedCosmetic).count();
                    String percentage = StringUtil.formatNumber((int) ((float) unlocked / total * 100));
                    String selected = Optional.ofNullable(cosmeticData.getTag()).orElse("None");

                    lore.add(CC.GRAY + "Unlocked: " + CC.WHITE + unlocked + '/' + total + CC.DARK_GRAY + " (" + percentage + "%)");
                    lore.add(CC.GRAY + "Selected: " + CC.WHITE + selected);
                    lore.add("");
                    lore.add(CC.GREEN + "Click to browse.");

                    return lore;
                })
                .build()
        ).onClick(() -> new TagSelectMenu(user).open());
    }
}
