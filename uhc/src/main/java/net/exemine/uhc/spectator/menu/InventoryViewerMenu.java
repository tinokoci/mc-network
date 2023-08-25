package net.exemine.uhc.spectator.menu;

import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.menu.Menu;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public class InventoryViewerMenu extends Menu<UHCUser> {

    public InventoryViewerMenu(UHCUser user, UHCUser target) {
        super(user, target.getColoredRealName() + CC.DARK_GRAY + "'s inventory", 5);
        setTarget(target);
    }

    @Override
    public void update() {
        PlayerInventory inventory = target.getInventory();

        IntStream.range(0, inventory.getArmorContents().length).forEach(i -> set(i, inventory.getArmorContents()[i]));
        IntStream.range(9, inventory.getContents().length).forEach(i -> set(i, inventory.getContents()[i]));
        IntStream.range(0, 9).forEach(i -> set(i + 36, inventory.getContents()[i]));

        set(8, new ItemBuilder()
                .setMaterial(Material.POTION)
                .setName(CC.PINK + "Potion Effects")
                .setLore(() -> {
                    List<String> lore = new ArrayList<>();
                    Collection<PotionEffect> potionEffects = target.getActivePotionEffects();
                    String prefix = ' ' + CC.GRAY + Lang.BULLET + ' ' + CC.WHITE;

                    if (potionEffects.isEmpty()) {
                        lore.add(prefix + "None");
                    } else {
                        potionEffects.forEach(potionEffect -> lore.add(prefix + StringUtil.formatEnumName(potionEffect.getType().getName())));
                    }
                    return lore;
                })
                .build());
    }
}
