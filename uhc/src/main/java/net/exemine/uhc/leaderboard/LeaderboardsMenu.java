package net.exemine.uhc.leaderboard;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.data.stat.TimedStatSpan;
import net.exemine.api.leaderboard.LeaderboardService;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class LeaderboardsMenu extends Menu<UHCUser> {

    private final UHCUserService userService;

    private final LeaderboardService<UHCData> leaderboardService;
    private final TimedStatSpan timedStatSpan;
    private final LeaderboardType type;

    public LeaderboardsMenu(UHCUser user, LeaderboardService<UHCData> leaderboardService, UHCUserService userService, TimedStatSpan timedStatSpan, LeaderboardType type) {
        super(user, CC.DARK_GRAY + timedStatSpan.getName() + ' ' + type.getName() + " Leaderboards", 6);
        this.userService = userService;
        this.leaderboardService = leaderboardService;
        this.timedStatSpan = timedStatSpan;
        this.type = type;
        setAutoSurround(true);
    }

    public LeaderboardsMenu(UHCUser user, LeaderboardService<UHCData> leaderboardService, UHCUserService userService) {
        this(user, leaderboardService, userService, TimedStatSpan.GLOBAL, LeaderboardType.COMBAT);
    }

    @Override
    public void update() {
        addExitItem();

        AtomicInteger index = new AtomicInteger(30);
        Arrays.stream(LeaderboardType.values()).forEach(type -> {
            boolean selected = this.type == type;

            set(index.getAndIncrement(), new ItemBuilder()
                    .setMaterial(selected ? Material.EMERALD_BLOCK : Material.IRON_BLOCK)
                    .setName(CC.PINK + type.getName())
                    .setLore("",
                            CC.GRAY + "Change the leaderboards",
                            CC.GRAY + "type to " + CC.WHITE + type.getName() + CC.GRAY + '.',
                            "",
                            selected
                                    ? CC.RED + "You are viewing these leaderboards."
                                    : CC.GREEN + "Click to view those leaderboards."
                    )
                    .build()
            ).onClick(() -> {
                if (selected) return;
                new LeaderboardsMenu(user, leaderboardService, userService, timedStatSpan, type).open();
            });
        });

        index.set(39);
        Arrays.stream(TimedStatSpan.values()).forEach(timedStatSpan -> {
            boolean selected = this.timedStatSpan == timedStatSpan;

            set(index.getAndIncrement(), new ItemBuilder()
                    .setMaterial(Material.INK_SACK)
                    .setName(CC.PINK + timedStatSpan.getName() + " Leaderboards")
                    .setDurability(selected ? 10 : 8)
                    .setLore("",
                            CC.GRAY + "Change the leaderboards",
                            CC.GRAY + "viewer to " + CC.WHITE + timedStatSpan.getName() + CC.GRAY + '.',
                            "",
                            selected
                                    ? CC.RED + "You are viewing these leaderboards."
                                    : CC.GREEN + "Click to view those leaderboards."
                    )
                    .build()
            ).onClick(() -> {
                if (selected) return;
                new LeaderboardsMenu(user, leaderboardService, userService, timedStatSpan, type).open();
            });
        });

        index.set(type == LeaderboardType.COMBAT || type == LeaderboardType.OTHER ? 11 : 10);

        IntStream.range(0, type.getKeys().size()).forEach(i -> {
            String key = type.getKeys().get(i);
            Material material = type.getMaterials().get(i);
            String name = (key.equals("kdr") ? "K/D" : StringUtil.formatEnumName(key.replace("-", "_")));

            set(index.getAndIncrement(), new ItemBuilder(material)
                    .setName(CC.PINK + name)
                    .setLore(() -> {
                        List<String> lore = new ArrayList<>(List.of(""));

                        (key.equals("kdr") ? leaderboardService.getByKeyRatio(key, timedStatSpan) : leaderboardService.getByKey(key, timedStatSpan)).forEach(info ->
                                lore.add(CC.GRAY + info.getPlacing() + ". " + info.getDisplayName() + CC.GRAY + " - " + CC.WHITE + info.getFormattedValue())
                        );
                        lore.add("");
                        lore.add(CC.GOLD + "You are ranked " + CC.PINK + '#' +
                                (key.equals("kdr") ? leaderboardService.getPlacingRatio(key, timedStatSpan, user.getUniqueId()) :
                                        leaderboardService.getPlacing(key, timedStatSpan, user.getUniqueId())) + CC.GOLD + '.');
                        return lore;
                    })
                    .build());
        });
    }
}
