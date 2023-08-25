package net.exemine.core.profile;

import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankInfo;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.menu.Menu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileViewMenu extends Menu<CoreUser> {

    public ProfileViewMenu(CoreUser user, CoreUser target) {
        super(user, getTitle(user, target), 5);
        setTarget(target);
        setAutoFill(true);
    }

    @Override
    public void update() {
        List<Rank> ranks = new ArrayList<>(
                target.isDisguised()
                        ? List.of()
                        : target.getBulkData().getActiveRankInfoList()
                        .stream()
                        .map(RankInfo::getRank)
                        .sorted(Comparator.comparingInt(Rank::getPriority))
                        .collect(Collectors.toList())
        );
        if (ranks.isEmpty()) ranks.add(Rank.DEFAULT);

        set(13, new ItemBuilder()
                .setMaterial(Material.NETHER_STAR)
                .setName(CC.PINK + "Rank" + (ranks.size() == 1 ? "" : "s"))
                .setLore(lore -> ranks.forEach(rank -> lore.add(Lang.LIST_PREFIX + rank.getDisplayName())))
                .build());
    }

    private static String getTitle(CoreUser user, CoreUser target) {
        if (user == target) return CC.DARK_GRAY + "Your Profile";
        return target.getColoredDisplayName() + CC.DARK_GRAY + "'s profile";
    }
}
