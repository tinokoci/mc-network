package net.exemine.core.punishment.menu.selector;

import lombok.Getter;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MuteMenu extends PaginatedMenu<CoreUser> {

    public MuteMenu(CoreUser user, CoreUser target) {
        super(user, CC.DARK_GRAY + "Select a mute reason:", 4, 2);
        setTarget(target);
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addExitItem();
        addReturnItem(new PunishMenu(user, target));
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        Arrays.stream(MuteReason.values()).forEach(reason -> {
            int punishmentsForReason = target.getBulkData().getPunishmentsByReason(PunishmentType.MUTE, reason.getName()).size();
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
                        lore.add(CC.GREEN + "Click to mute for " + StringUtil.getOrdinal(Math.min(punishmentsForReason + 1, maxPunishments)) + " offence.");
                    })
                    .build()
            ).onClick(() -> {
                int durationIndex = Math.min(punishmentsForReason, maxPunishments - 1);
                user.performCommand("mute " + target.getRealName() + ' ' + TimeUtil.getCharDuration(reason.getScaling().get(durationIndex)) + " " + reason.getName());
                close(false);
            });
        });
    }

    @Getter
    private enum MuteReason {

        SPAM("Spam", Material.BOOK_AND_QUILL, TimeUtil.MINUTE * 30, TimeUtil.HOUR * 3, TimeUtil.DAY, TimeUtil.DAY * 3),
        HELPOP_ABUSE("Helpop Abuse", Material.PAPER, TimeUtil.MINUTE * 30, TimeUtil.HOUR * 3, TimeUtil.DAY, TimeUtil.DAY * 3),
        HACKUSATING("Hackusating", Material.GOLD_SWORD, TimeUtil.MINUTE * 30, TimeUtil.HOUR * 3, TimeUtil.DAY, TimeUtil.DAY * 3),
        TOXICITY("Toxicity", Material.ROTTEN_FLESH, TimeUtil.HOUR * 3, TimeUtil.DAY, TimeUtil.DAY * 3, TimeUtil.WEEK),
        RACISM("Racism", Material.COAL, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2, TimeUtil.MONTH),
        ADVERTISING("Advertising", Material.COMPASS, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2, TimeUtil.MONTH),
        DEATH_THREATS_WISHES("Death Threats/Wishes", Material.BOW, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2, TimeUtil.MONTH),
        INAPPROPRIATE_CONTENT("Inappropriate Content", Material.ANVIL, TimeUtil.DAY, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2),
        SPOILING("Spoiling", Material.EYE_OF_ENDER, TimeUtil.DAY, TimeUtil.DAY * 3, TimeUtil.WEEK, TimeUtil.WEEK * 2);

        private final String name;
        private final Material material;
        private final List<Long> scaling;

        MuteReason(String name, Material material, Long... scaling) {
            this.name = name;
            this.material = material;
            this.scaling = List.of(scaling);
        }
    }
}