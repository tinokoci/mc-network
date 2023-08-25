package net.exemine.core.match;

import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.model.ScenarioName;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class UpcomingGamesMenu extends PaginatedMenu<CoreUser> {

    private final List<UHCMatch> matches;

    public UpcomingGamesMenu(CoreUser user, List<UHCMatch> matches) {
        super(user, CC.DARK_GRAY + "Upcoming UHC matches", 4, 2);
        this.matches = matches;
        setAutoSurround(true);
        setUpdateAfterClick(false);
        setAsync(true);
    }

    @Override
    public void global() {
        addExitItem();
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();
        matches.forEach(match -> paginate(index.getAndIncrement(), new ItemBuilder()
                .setMaterial(Material.GOLDEN_APPLE)
                .setName(CC.PINK + "UHC Match")
                .setLore(lore -> {
                    lore.add(CC.DARK_GRAY + TimeUtil.getDate(match.getStartTime()));
                    lore.add("");
                    lore.add(CC.GRAY + "Game ID: " + CC.GOLD + match.getId());
                    lore.add(CC.GRAY + "Whitelist off in: " + CC.GOLD + (TimeUtil.getNormalDuration(match.getStartTime() - System.currentTimeMillis())));

                    if (match.getHostUuid() != null) {
                        AtomicReference<String> hostName = new AtomicReference<>("Unknown");
                        user.getUserService().fetch(match.getHostUuid()).ifPresent(host -> hostName.set(host.getColoredRealName()));
                        lore.add(CC.GRAY + "Host: " + CC.WHITE + hostName.get());
                    }
                    if (match.hasSupervisor()) {
                        AtomicReference<String> supervisorName = new AtomicReference<>("Unknown");
                        user.getUserService().fetch(match.getSupervisorUuid()).ifPresent(supervisor -> supervisorName.set(supervisor.getColoredRealName()));
                        lore.add(CC.GRAY + "Supervisor: " + CC.WHITE + supervisorName.get());
                    }
                    lore.add("");
                    lore.add(CC.GRAY + "Mode: " + CC.GOLD + match.getMode());
                    lore.add(CC.GRAY + "Nether: " + StringUtil.formatBooleanStatus(match.isNether()));
                    lore.add("");
                    lore.add(CC.GRAY + "Scenarios:");

                    Set<ScenarioName> scenarios = match.getScenarios();
                    if (scenarios.isEmpty()) {
                        lore.add(Lang.LIST_PREFIX + CC.RED + "Vanilla");
                    } else {
                        match.getScenarios().forEach(scenario -> lore.add(Lang.LIST_PREFIX + CC.GREEN + scenario.getName()));
                    }
                })
                .build()));
    }
}
