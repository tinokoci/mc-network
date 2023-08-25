package net.exemine.core.match.host;

import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.model.ScenarioName;
import net.exemine.api.util.EnumUtil;
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

public class HostHistoryMenu extends PaginatedMenu<CoreUser> {

    private final List<UHCMatch> matches;

    public HostHistoryMenu(CoreUser user, CoreUser target, List<UHCMatch> matches) {
        super(user, CC.DARK_GRAY + target.getColoredRealName() + CC.DARK_GRAY + "'s UHC matches", 4, 2);
        this.matches = matches;
        setTarget(target);
        setAutoSurround(true);
        setUpdateAfterClick(false);
        setAsync(true);
    }

    @Override
    public void global() {
        addExitItem();
        set(size - 5, new ItemBuilder()
                .setMaterial(Material.WATCH)
                .setName(CC.PINK + "Hosting Information")
                .setLore(lore -> {
                    lore.add("");
                    lore.add(CC.GRAY + "Hosted Matches: " + CC.GOLD + matches.size());
                    lore.add(CC.GRAY + "Most Players: " + CC.GOLD + matches
                            .stream()
                            .mapToInt(UHCMatch::getInitialPlayerCount)
                            .max()
                            .orElse(0)
                    );
                    lore.add(CC.GRAY + "Total Thanks Received: " + CC.GOLD + matches
                            .stream()
                            .mapToInt(UHCMatch::getThanksCount)
                            .sum());
                })
                .build());
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
                    lore.add(CC.GRAY + "Duration: " + CC.GOLD + TimeUtil.getClockTime(match.getDuration()));
                    lore.add(CC.GRAY + "State: " + CC.GOLD + EnumUtil.getName(match.getState()));
                    lore.add("");

                    if (match.hasSupervisor()) {
                        AtomicReference<String> supervisorName = new AtomicReference<>("Unknown");
                        user.getUserService().fetch(match.getSupervisorUuid()).ifPresent(supervisor -> supervisorName.set(supervisor.getColoredRealName()));
                        lore.add(CC.GRAY + "Supervisor: " + CC.WHITE + supervisorName.get());
                        lore.add("");
                    }
                    lore.add(CC.GRAY + "Mode: " + CC.GOLD + match.getMode());
                    lore.add(CC.GRAY + "Players: " + CC.GOLD + match.getInitialPlayerCount());
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
