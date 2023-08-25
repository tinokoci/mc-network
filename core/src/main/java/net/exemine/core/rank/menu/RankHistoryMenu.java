package net.exemine.core.rank.menu;

import net.exemine.api.rank.RankInfo;
import net.exemine.api.util.Executor;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import net.exemine.core.menu.confirm.ConfirmMenu;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.UserUtil;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import org.bukkit.Material;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class RankHistoryMenu extends PaginatedMenu<CoreUser> {

    public RankHistoryMenu(CoreUser user, CoreUser target) {
        super(user, target.getColoredRealName() + CC.DARK_GRAY + "'s ranks", 3, 1);
        setTarget(target);
        setAsync(true);
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addExitItem();
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        target.getBulkData().getRankInfoList()
                .stream()
                .sorted(Comparator
                        .comparing(RankInfo::isActive, Comparator.reverseOrder())
                        .thenComparingInt(grant -> grant.getRank().getPriority())
                        .thenComparingInt(RankInfo::getIndex))
                .forEach(rankInfo -> paginate(index.getAndIncrement(), new ItemBuilder()
                        .setMaterial(Material.WOOL)
                        .setDurability(rankInfo.isActive() ? ItemUtil.getGreen() : ItemUtil.getRed())
                        .setName((rankInfo.isActive() ? CC.GREEN : CC.RED) + "Rank #" + rankInfo.getIndex())
                        .setLore(lore -> {
                            boolean canRemoveRank = user.canModifyRank(rankInfo.getRank());

                            lore.add("");
                            lore.add(CC.GRAY + "Rank: " + rankInfo.getRank().getDisplayName());
                            lore.add(CC.GRAY + "Added by: " + CC.WHITE + UserUtil.getFormattedIssuer(rankInfo.getAddedBy()));
                            lore.add(CC.GRAY + "Added at: " + CC.WHITE + TimeUtil.getDate(rankInfo.getAddedAt()));
                            lore.add(CC.GRAY + "Duration: " + rankInfo.getFormattedDuration());
                            lore.add(CC.GRAY + "Reason: " + CC.WHITE + rankInfo.getAddedReason());

                            if (!rankInfo.isActive()) {
                                lore.add("");
                                lore.add(CC.GRAY + "Removed by: " + CC.WHITE + UserUtil.getFormattedIssuer(rankInfo.getRemovedBy()));
                                lore.add(CC.GRAY + "Removed at: " + CC.WHITE + TimeUtil.getDate(rankInfo.getRemovedAt()));
                            }
                            lore.add("");
                            lore.add(!rankInfo.isActive() ? CC.RED + "This rank is no longer active."
                                    : canRemoveRank ? CC.GREEN + "Click to remove this rank."
                                    : CC.RED + "You cannot remove this rank.");
                        })
                        .build()
                ).onClick(() -> {
                    if (rankInfo.isRemoved() || !user.canModifyRank(rankInfo.getRank())) return;

                    new ConfirmMenu(user, true, confirm -> {
                        if (!confirm) return;

                        Executor.schedule(() -> {
                            Core.get().getRankService().removeRank(target.getData(), target.getBulkData(), rankInfo, user.getUniqueId());
                            user.sendMessage(CC.PURPLE + "[Rank] " + CC.GRAY + "You've removed " + rankInfo.getRank().getDisplayName() + CC.GRAY + " from " + target.getColoredRealName() + CC.GRAY + '.');
                        }).runAsync();
                    }).open();
                }));
    }
}