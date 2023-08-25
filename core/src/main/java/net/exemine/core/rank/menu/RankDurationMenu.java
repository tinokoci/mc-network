package net.exemine.core.rank.menu;

import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.rank.procedure.RankProcedure;
import net.exemine.core.rank.procedure.RankProcedureDuration;
import net.exemine.core.rank.procedure.RankProcedureState;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class RankDurationMenu extends Menu<CoreUser> {

    private final RankProcedure procedure;

    public RankDurationMenu(CoreUser user) {
        super(user, CC.DARK_GRAY + "Select a duration:", 4);
        procedure = RankProcedure.getProcedure(user);
        setAutoSurround(true);
    }

    @Override
    public void update() {
        addExitItem();
        addReturnItem(new RankSelectMenu(user));

        AtomicInteger index = new AtomicInteger(10);

        Arrays.stream(RankProcedureDuration.values()).forEach(duration -> {
            set(index.getAndIncrement(), new ItemBuilder(Material.PAPER)
                    .setName(CC.PINK + duration.getName())
                    .setLore("",
                            CC.GRAY + "Player: " + CC.WHITE + procedure.getTarget().getColoredRealName(),
                            CC.GRAY + "Rank: " + procedure.getRank().getDisplayName(),
                            "",
                            CC.GREEN + "Click to select this duration!"
                    ).build()
            ).onClick(() -> {
                procedure.setDuration(TimeUtil.getMillisFromInput(duration.getFormat()));
                procedure.setState(RankProcedureState.REASON);
                user.sendMessage(CC.PURPLE + "[Rank] " + CC.GRAY + CC.GRAY + "You have selected " + CC.GOLD + duration.getName() + CC.GRAY + " as the duration.");

                new RankReasonMenu(user).open();
            });
            if (index.get() == 13) index.getAndIncrement();
        });

        set(22, new ItemBuilder(Material.BOOK_AND_QUILL)
                .setName(CC.PINK + "Custom")
                .setLore("",
                        CC.GRAY + "Player: " + CC.WHITE + procedure.getTarget().getColoredRealName(),
                        CC.GRAY + "Rank: " + procedure.getRank().getDisplayName(),
                        "",
                        CC.GREEN + "Click to input a custom duration!"
                ).build()
        ).onClick(() -> {
            user.sendMessage('\n' + CC.PINK + "Please input a custom duration in chat.");
            user.sendMessage(CC.ITALIC_GRAY + "(Example: 2h30m -> 2 hours and 30 minutes)\n ");
            close(false);
        });
    }

    @Override
    public void onClose() {
        procedure.cancel();
    }
}
