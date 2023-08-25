package net.exemine.core.rank.menu;

import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.rank.procedure.RankProcedure;
import net.exemine.core.rank.procedure.RankProcedureState;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class RankSelectMenu extends PaginatedMenu<CoreUser> {

    private final RankProcedure procedure;

    public RankSelectMenu(CoreUser user) {
        super(user, CC.DARK_GRAY + "Select a rank:", 4, 2);
        procedure = RankProcedure.getProcedure(user);
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addExitItem();
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        Arrays.stream(Rank.values())
                .filter(rank -> rank != Rank.DEFAULT)
                .sorted(Comparator.comparingInt(Rank::ordinal))
                .forEach(rank -> {
                    boolean canGrantRank = user.canModifyRank(rank);

                    paginate(index.getAndIncrement(), new ItemBuilder(Material.WOOL)
                            .setDurability(ItemUtil.getWoolData(rank.getColor()))
                            .setName(rank.getDisplayName())
                            .setLore("",
                                    CC.GRAY + "Player: " + CC.WHITE + procedure.getTarget().getRealName(),
                                    "",
                                    canGrantRank
                                            ? CC.GREEN + "Click to select this rank!"
                                            : CC.RED + "You cannot select this rank."
                            ).build()
                    ).onClick(() -> {
                        if (!canGrantRank) return;

                        procedure.setRank(rank);
                        procedure.setState(RankProcedureState.DURATION);
                        user.sendMessage(CC.PURPLE + "[Rank] " + CC.GRAY + "You have selected " + rank.getDisplayName() + CC.GRAY + " rank.");

                        new RankDurationMenu(user).open();
                    });
                });
    }

    @Override
    public void onClose() {
        procedure.cancel();
    }
}
