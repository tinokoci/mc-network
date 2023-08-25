package net.exemine.core.punishment.menu;

import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;

public class PunishmentListMenu extends Menu<CoreUser> {

    public PunishmentListMenu(CoreUser user, CoreUser target) {
        super(user, target.getColoredRealName() + CC.DARK_GRAY + "'s punishments", 4);
        setTarget(target);
        setAutoSurround(true);
    }

    @Override
    public void update() {
        addExitItem();

        int[] indexes = {11, 13, 15, 21, 23};
        BulkData data = target.getBulkData();

        for (int i = 0; i < indexes.length; i++) {
            PunishmentType type = PunishmentType.values()[i];
            boolean check = data.getPunishmentsByType(type).isEmpty();

            set(indexes[i], new ItemBuilder(Material.valueOf(type.getMaterial()))
                    .setName(type.getMenuColor() + type.getName() + 's')
                    .setLore(lore -> {
                        lore.add("");
                        lore.add(CC.GRAY + StringUtils.capitalize(type.getFormat()) + ": " + StringUtil.formatBooleanStatus(data.getActivePunishment(type) != null));
                        lore.add(CC.GRAY + "Total " + StringUtils.capitalize(type.getPlural()) + ": " + CC.WHITE + data.getPunishmentsByType(type).size());
                        lore.add("");
                        lore.add(check
                                ? CC.RED + "Player was never " + type.getFormat() + '.'
                                : CC.GREEN + "Click to see player's " + type.getPlural() + ".");
                    })
                    .build()
            ).onClick(() -> {
                if (check) return;
                new PunishmentTypeMenu(user, target, type).open();
            });
        }
    }
}
