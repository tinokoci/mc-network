package net.exemine.core.punishment.menu;

import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.UserUtil;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.util.concurrent.atomic.AtomicInteger;

public class PunishmentTypeMenu extends PaginatedMenu<CoreUser> {

    private final PunishmentType type;

    public PunishmentTypeMenu(CoreUser user, CoreUser target, PunishmentType type) {
        super(user, target.getColoredRealName() + CC.DARK_GRAY + "'s " + type.getPlural(), 3, 1);
        this.type = type;

        setTarget(target);
        setAsync(true);
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addExitItem();
        addReturnItem(new PunishmentListMenu(user, target));
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        target.getBulkData().getPunishmentsByType(type).forEach(punishment -> paginate(index.getAndIncrement(), new ItemBuilder()
                .setMaterial(punishment.isActive() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
                .setName((punishment.isActive() ? CC.GREEN : CC.RED) + type.getName() + " #" + punishment.getIndex())
                .setLore(lore -> {
                    lore.add("");
                    lore.add(CC.GRAY + "Added by: " + CC.WHITE + UserUtil.getFormattedIssuer(punishment.getAddedBy()));
                    lore.add(CC.GRAY + "Added at: " + CC.WHITE + TimeUtil.getDate(punishment.getAddedAt()));
                    lore.add(CC.GRAY + "Added Reason: " + CC.WHITE + punishment.getAddedReason());
                    lore.add(CC.GRAY + "Duration: " + punishment.getFormattedDuration());

                    if (!punishment.isActive() && punishment.getType() != PunishmentType.KICK) {
                        lore.add("");
                        lore.add(CC.GRAY + "Removed by: " + CC.WHITE + UserUtil.getFormattedIssuer(punishment.getRemovedBy()));
                        lore.add(CC.GRAY + "Removed at: " + CC.WHITE + TimeUtil.getDate(punishment.getRemovedAt()));
                        lore.add(CC.GRAY + "Removed Reason: " + CC.WHITE + punishment.getRemovedReason());
                    }
                    lore.add("");
                    lore.add(punishment.isActive() ? CC.GREEN + "This punishment is active!" : CC.RED + "This punishment is no longer active!");
                })
                .build()
        ));
    }
}
