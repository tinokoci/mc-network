package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.data.stat.TimedStatSpan;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.menu.Menu;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StatsCommand extends BaseCommand<UHCUser, UHCData> {

    public StatsCommand() {
        super(List.of("stats"));
        setAsync(true);
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase(user.getRealName())) {
            new StatsMenu(user, user).open();
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(
                target -> new StatsMenu(user, target).open(),
                () -> user.sendMessage(Lang.USER_NEVER_PLAYED)
        );
    }

    private static class StatsMenu extends Menu<UHCUser> {

        private final TimedStatSpan timedStatSpan;

        public StatsMenu(UHCUser user, UHCUser target, TimedStatSpan timedStatSpan) {
            super(user, target.getColoredDisplayName() + CC.DARK_GRAY + "'s " + timedStatSpan.name().toLowerCase() + " stats", 5);
            this.timedStatSpan = timedStatSpan;
            setTarget(target);
            setAutoSurround(true);
        }

        public StatsMenu(UHCUser user, UHCUser target) {
            this(user, target, TimedStatSpan.GLOBAL);
        }

        @Override
        public void update() {
            addExitItem();

            UHCData data = target.getData();
            AtomicInteger index = new AtomicInteger(30);

            Arrays.stream(TimedStatSpan.values()).forEach(timedStatSpan -> {
                boolean selected = this.timedStatSpan == timedStatSpan;

                set(index.getAndIncrement(), new ItemBuilder()
                        .setMaterial(Material.INK_SACK)
                        .setName(CC.PINK + timedStatSpan.getName() + " Stats")
                        .setDurability(selected ? 10 : 8)
                        .setLore("",
                                CC.GRAY + "Change the stats viewer",
                                CC.GRAY + "to " + CC.WHITE + timedStatSpan.getName() + CC.GRAY + '.',
                                "",
                                selected
                                        ? CC.RED + "You are viewing these stats."
                                        : CC.GREEN + "Click to view those stats."
                        )
                        .build()
                ).onClick(() -> {
                    if (selected) return;
                    new StatsMenu(user, target, timedStatSpan).open();
                });
            });

            set(11, new ItemBuilder()
                    .setMaterial(Material.DIAMOND_SWORD)
                    .setName(CC.PINK + "Combat Stats")
                    .setLore("",
                            CC.GRAY + "Elo: " + CC.WHITE + StringUtil.formatNumber(data.getElo().getInTimeSpan(timedStatSpan)), // TODO: Adjust color depending on elo division
                            CC.GRAY + "Wins: " + CC.WHITE + StringUtil.formatNumber(data.getWins().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Kills: " + CC.WHITE + StringUtil.formatNumber(data.getKills().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Deaths: " + CC.WHITE + StringUtil.formatNumber(data.getDeaths().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "K/D: " + CC.WHITE + StringUtil.formatRatio(data.getKills().getInTimeSpan(timedStatSpan),
                                    data.getDeaths().getInTimeSpan(timedStatSpan))
                    ).build());
            set(13, new ItemBuilder()
                    .setMaterial(Material.DIAMOND_ORE)
                    .setName(CC.PINK + "Mining Stats")
                    .setLore("",
                            CC.GRAY + "Diamonds: " + CC.WHITE + StringUtil.formatNumber(data.getMinedDiamonds().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Gold: " + CC.WHITE + StringUtil.formatNumber(data.getMinedGold().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Iron: " + CC.WHITE + StringUtil.formatNumber(data.getMinedIron().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Redstone: " + CC.WHITE + StringUtil.formatNumber(data.getMinedRedstone().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Lapis: " + CC.WHITE + StringUtil.formatNumber(data.getMinedLapis().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Coal: " + CC.WHITE + StringUtil.formatNumber(data.getMinedCoal().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Quartz: " + CC.WHITE + StringUtil.formatNumber(data.getMinedQuartz().getInTimeSpan(timedStatSpan))
                    ).build());
            set(15, new ItemBuilder()
                    .setMaterial(Material.ENCHANTMENT_TABLE)
                    .setName(CC.PINK + "Other Stats")
                    .setLore("",
                            CC.GRAY + "Games Played: " + CC.WHITE + StringUtil.formatNumber(data.getGamesPlayed().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Top5s: " + CC.WHITE + StringUtil.formatNumber(data.getTop5s().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Carried Wins: " + CC.WHITE + StringUtil.formatNumber(data.getCarriedWins().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Levels Earned: " + CC.WHITE + StringUtil.formatNumber(data.getLevelsEarned().getInTimeSpan(timedStatSpan)),
                            CC.GRAY + "Nethers Entered: " + CC.WHITE + StringUtil.formatNumber(data.getNethersEntered().getInTimeSpan(timedStatSpan))
                    ).build());
        }
    }
}
