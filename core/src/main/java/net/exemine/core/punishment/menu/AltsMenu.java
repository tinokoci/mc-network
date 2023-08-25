package net.exemine.core.punishment.menu;

import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.Instance;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.Executor;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class AltsMenu extends PaginatedMenu<CoreUser> {

    private final InstanceService instanceService;

    public AltsMenu(CoreUser user, CoreUser target) {
        super(user, target.getColoredRealName() + CC.DARK_GRAY + "'s alts", 3, 1);
        this.instanceService = user.getPlugin().getInstanceService();

        setTarget(target);
        setAsync(true);
        setAutoSurround(true);
        setAutoUpdate(true);
    }

    @Override
    public void global() {
        addExitItem();
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();
        CoreData data = target.getData();

        target.getAltAccounts(true)
                .stream()
                .sorted(Comparator.comparing(alt -> data.isAltIgnored(alt.getUniqueId())))
                .forEach(alt -> paginate(index.getAndIncrement(), ItemBuilder.getPlayerHead(alt.getRealName())
                        .setName(data.isAltIgnored(alt.getUniqueId())
                                ? CC.DARK_GRAY + alt.getRealName() + " (Ignored)"
                                : alt.getColoredRealName()
                        )
                        .setLore(lore -> {
                            BulkData altBulkData = alt.getBulkData();
                            lore.add("");
                            lore.add(CC.GRAY + "Banned: " + StringUtil.formatBooleanStatus(altBulkData.getActivePunishment(PunishmentType.BAN, PunishmentType.IP_BAN) != null));
                            lore.add(CC.GRAY + "Muted: " + StringUtil.formatBooleanStatus(altBulkData.getActivePunishment(PunishmentType.MUTE) != null));
                            lore.add("");
                            lore.add(CC.GRAY + "Rank: " + alt.getRank().getDisplayName());
                            lore.add(CC.GRAY + "First Joined:");
                            lore.add(Lang.LIST_PREFIX + CC.WHITE + TimeUtil.getDate(alt.getData().getFirstJoined()));
                            lore.add("");

                            Instance server = instanceService.getInstanceByUser(alt.getRealName());

                            lore.add(server == null ? CC.RED + "User is offline." : CC.GREEN + "User is online on " + server.getName() + '.');

                            if (user.isEqualOrAbove(Rank.ADMIN)) {
                                lore.add("");
                                lore.add(CC.GOLD + "Click to toggle ignore status.");
                            }
                        })
                        .build()
                ).onClick(() -> {
                    if (!user.isEqualOrAbove(Rank.ADMIN)) return;

                    // We need to update ignored alts for both accounts
                    boolean isAdded = data.isAltIgnored(alt.getUniqueId());

                    Executor.schedule(() -> {
                        target.toggleAltIgnore(target, alt, isAdded);
                        alt.toggleAltIgnore(alt, target, isAdded);
                    }).runAsync();
                }));
    }
}
