package net.exemine.core.punishment.menu.selector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.util.EnumUtil;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BanMenu extends PaginatedMenu<CoreUser> {

    private BanCategory category = BanCategory.GENERAL;

    public BanMenu(CoreUser user, CoreUser target) {
        super(user, CC.DARK_GRAY + "Select a ban reason:", 4, 2);
        setTarget(target);
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addExitItem();
        addReturnItem(new PunishMenu(user, target));
        set(size - 5, new ItemBuilder()
                .setMaterial(category.getMaterial())
                .setName(CC.PINK + "Filter")
                .setLore(lore -> Arrays.stream(BanCategory.values()).forEach(category ->
                        lore.add(Lang.LIST_PREFIX + (this.category == category ? CC.GREEN : CC.WHITE) + category.getName()))
                )
                .build()
        ).onClick(() -> {
            category = EnumUtil.getNext(category);
            open();
        });
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        Arrays.stream(BanReason.values())
                .filter(reason -> reason.getCategory() == category)
                .forEach(reason -> {
                    int punishmentsForReason = target.getBulkData().getPunishmentsByReason(PunishmentType.BAN, reason.getName()).size();
                    int maxPunishments = reason.getScaling().size();

                    paginate(index.getAndIncrement(), new ItemBuilder()
                            .setMaterial(reason.getMaterial())
                            .setName(CC.PINK + reason.getName())
                            .setLore(lore -> {
                                lore.add("");

                                AtomicInteger scalingIndex = new AtomicInteger(0);
                                reason.getScaling().forEach(duration -> {
                                    int i = scalingIndex.getAndIncrement();
                                    String ordinal = StringUtil.getOrdinal(i + 1);
                                    String colorCode = i == punishmentsForReason ? CC.RED
                                            : i < punishmentsForReason || punishmentsForReason >= maxPunishments ? CC.STRIKETHROUGH_DARK_GRAY
                                            : CC.GOLD;
                                    lore.add(CC.GRAY + ordinal + " offence: " + colorCode + TimeUtil.getNormalDuration(duration));
                                });
                                lore.add("");
                                lore.add(CC.GREEN + "Click to ban for " + StringUtil.getOrdinal(Math.min(punishmentsForReason + 1, maxPunishments)) + " offence.");
                            })
                            .build()
                    ).onClick(() -> {
                        int durationIndex = Math.min(punishmentsForReason, maxPunishments - 1);
                        user.performCommand("ban " + target.getRealName() + ' ' + TimeUtil.getCharDuration(reason.getScaling().get(durationIndex)) + " " + reason.getName());
                        close(false);
                    });
                });
    }

    @RequiredArgsConstructor
    @Getter
    private enum BanCategory {

        GENERAL("General", Material.IRON_FENCE),
        UHC("UHC", Material.GOLDEN_APPLE);

        private final String name;
        private final Material material;
    }

    @Getter
    private enum BanReason {

        CHEATING(BanCategory.GENERAL, "Cheating", Material.DIAMOND_SWORD, TimeUtil.MONTH, TimeUtil.MONTH * 3, Long.MAX_VALUE),
        IP_BAN_EVASION(BanCategory.GENERAL, "IP Ban Evasion", Material.SKULL_ITEM, Long.MAX_VALUE),
        VPN_USAGE(BanCategory.GENERAL, "VPN Usage", Material.COMPASS, Long.MAX_VALUE),
        DOXING(BanCategory.GENERAL, "Doxing", Material.NAME_TAG, Long.MAX_VALUE),
        DDOS_THREATS(BanCategory.GENERAL, "DDoS Threats", Material.ANVIL, Long.MAX_VALUE),
        TEAMING(BanCategory.GENERAL, "Teaming", Material.GOLD_CHESTPLATE, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2, TimeUtil.MONTH),
        CAMPING(BanCategory.GENERAL, "Camping", Material.COBBLE_WALL, TimeUtil.DAY, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2),
        STATS_MANIPULATION(BanCategory.GENERAL, "Stats Manipulation", Material.NETHER_STAR, TimeUtil.WEEK, TimeUtil.WEEK * 2, TimeUtil.MONTH, TimeUtil.MONTH * 3),
        BUG_EXPLOITATION(BanCategory.GENERAL, "Bug Exploitation", Material.QUARTZ, TimeUtil.DAY, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2),
        GRIEFING(BanCategory.GENERAL, "Griefing", Material.TNT, TimeUtil.DAY, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2),

        XRAY(BanCategory.UHC, "X-Ray", Material.DIAMOND_ORE, TimeUtil.MONTH, TimeUtil.MONTH * 3, Long.MAX_VALUE),
        EXCESSIVE_STALKING(BanCategory.UHC, "Excessive Stalking", Material.LAVA_BUCKET, TimeUtil.DAY, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2),
        PORTAL_TRAPPING(BanCategory.UHC, "Portal Trapping", Material.NETHER_FENCE, TimeUtil.DAY, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2),
        SKY_BASING(BanCategory.UHC, "Sky Basing", Material.FEATHER, TimeUtil.DAY, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2),
        DND_TAG_ABUSE(BanCategory.UHC, "DnD Tag Abuse", Material.GOLD_SWORD, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2, TimeUtil.MONTH),
        INTERFERING_WITH_DND_ASSIGNED(BanCategory.UHC, "Interfering with DnD/Assigned Fights", Material.IRON_SWORD, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2, TimeUtil.MONTH),
        SACRIFICING(BanCategory.UHC, "Sacrificing", Material.GOLDEN_APPLE, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2, TimeUtil.MONTH),
        ASSISTING_IN_SACRIFICING(BanCategory.UHC, "Assisting in Sacrificing", Material.GOLDEN_CARROT, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2, TimeUtil.MONTH),
        I_PVP(BanCategory.UHC, "iPvP", Material.FLINT_AND_STEEL, TimeUtil.DAY, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2);

        private final BanCategory category;
        private final String name;
        private final Material material;
        private final List<Long> scaling;

        BanReason(BanCategory category, String name, Material material, Long... scaling) {
            this.category = category;
            this.name = name;
            this.material = material;
            this.scaling = List.of(scaling);
        }
    }
}