package net.exemine.core.punishment.menu.selector;

import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

public class PunishMenu extends Menu<CoreUser> {

    public PunishMenu(CoreUser user, CoreUser target) {
        super(user, CC.DARK_GRAY + "Select a punishment type:", 3);
        setTarget(target);
        setAutoSurround(true);
    }

    @Override
    public void update() {
        addExitItem();

        set(12, new ItemBuilder(Material.GOLD_BARDING)
                .setName(CC.PINK + "Ban")
                .setLore(CC.GRAY + "Click to open the ban selector.")
                .build()).onClick(() -> new BanMenu(user, target).open());

        set(14, new ItemBuilder(Material.PAPER)
                .setName(CC.PINK + "Mute")
                .setLore(CC.GRAY + "Click to open the mute selector.")
                .build()).onClick(() -> new MuteMenu(user, target).open());
    }
}
