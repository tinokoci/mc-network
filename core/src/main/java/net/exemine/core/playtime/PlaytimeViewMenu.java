package net.exemine.core.playtime;

import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.stat.TimedStatSpan;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;
import net.exemine.core.util.item.Glow;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class PlaytimeViewMenu extends Menu<CoreUser> {

    public PlaytimeViewMenu(CoreUser user, CoreUser target) {
        super(user, CC.GRAY + target.getColoredRealName() + CC.DARK_GRAY + "'s playtime", 5);
        setTarget(target);
        setAutoUpdate(true);
    }

    @Override
    public void update() {
        addExitItem();

        BulkData bulkData = target.getBulkData();
        AtomicInteger index = new AtomicInteger(10);
        IntStream.range(0, 7).forEach(i -> {
            Period period = TimeUtil.getCurrentPeriod().minusDays(i);

            List<String> lore = new ArrayList<>(List.of(""));
            Arrays.stream(InstanceType.values())
                    .filter(type -> type == InstanceType.HUB || type == InstanceType.UHC)
                    .forEach(type -> {
                        long sessionLoginTime = target.getSessionLoginTime(period.equals(TimeUtil.getCurrentPeriod()) && InstanceUtil.isType(type));
                        long typePlaytime = bulkData.getPlayTime(period, sessionLoginTime, type);
                        lore.add(CC.GRAY + type.getName() + " playtime: " + CC.GOLD + TimeUtil.getPlayTime(typePlaytime));
                    });
            lore.add("");
            long sessionLoginTime = target.getSessionLoginTime(period.equals(TimeUtil.getCurrentPeriod()));
            long totalPlaytime = bulkData.getPlayTime(period, sessionLoginTime);
            lore.add(CC.GRAY + "Total playtime: " + CC.GREEN + TimeUtil.getPlayTime(totalPlaytime));

            set(index.getAndIncrement(), new ItemBuilder()
                    .setMaterial(Material.WATCH)
                    .setAmount(i)
                    .addEnchantment(i == 0 ? Glow.get() : null)
                    .setName(CC.PINK + indexToText(i))
                    .setLore(lore)
                    .build());
        });

        set(31, new ItemBuilder()
                .setMaterial(Material.SIGN)
                .setName(CC.PINK + "Playtime Overview")
                .setLore(lore -> {
                    lore.add("");
                    Arrays.stream(InstanceType.values())
                            .filter(type -> type == InstanceType.HUB || type == InstanceType.UHC)
                            .forEach(type -> {
                                long sessionLoginTime = target.getSessionLoginTime(InstanceUtil.isType(type));
                                lore.add(CC.GRAY + type.getName() + " playtime this week: " + CC.GOLD + TimeUtil.getPlayTime(bulkData.getPlayTime(TimedStatSpan.WEEKLY, sessionLoginTime, type)));
                                lore.add(CC.GRAY + type.getName() + " playtime this month: " + CC.GOLD + TimeUtil.getPlayTime(bulkData.getPlayTime(TimedStatSpan.MONTHLY, sessionLoginTime, type)));
                                lore.add(CC.GRAY + type.getName() + " playtime overall: " + CC.GOLD + TimeUtil.getPlayTime(bulkData.getPlayTime(TimedStatSpan.GLOBAL, sessionLoginTime, type)));
                                lore.add("");
                            });
                    long sessionLoginTime = target.getSessionLoginTime();
                    lore.add(CC.GRAY + "Playtime this week: " + CC.GREEN + TimeUtil.getPlayTime(bulkData.getPlayTime(TimedStatSpan.WEEKLY, sessionLoginTime)));
                    lore.add(CC.GRAY + "Playtime this month: " + CC.GREEN + TimeUtil.getPlayTime(bulkData.getPlayTime(TimedStatSpan.MONTHLY, sessionLoginTime)));
                    lore.add(CC.GRAY + "Total playtime: " + CC.GREEN + TimeUtil.getPlayTime(bulkData.getPlayTime(sessionLoginTime)));
                })
                .build());
    }

    private String indexToText(int index) {
        switch (index) {
            case 0:
                return "Today";
            case 1:
                return "Yesterday";
            default:
                return index + " days ago";
        }
    }
}
