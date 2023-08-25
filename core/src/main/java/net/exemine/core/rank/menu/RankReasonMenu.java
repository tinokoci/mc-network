package net.exemine.core.rank.menu;

import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.rank.procedure.RankProcedure;
import net.exemine.core.rank.procedure.RankProcedureReason;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class RankReasonMenu extends Menu<CoreUser> {

    private final RankProcedure procedure;

    public RankReasonMenu(CoreUser user) {
        super(user, CC.DARK_GRAY + "Select a reason:", 4);
        procedure = RankProcedure.getProcedure(user);
        setAutoSurround(true);
    }

    @Override
    public void update() {
        addExitItem();
        addReturnItem(new RankDurationMenu(user));

        AtomicInteger index = new AtomicInteger(10);

        Arrays.stream(RankProcedureReason.values()).forEach(reason -> {
            set(index.getAndIncrement(), new ItemBuilder(Material.PAPER)
                    .setName(CC.PINK + reason.getName())
                    .setLore("",
                            CC.GRAY + "Player: " + CC.WHITE + procedure.getTarget().getRealName(),
                            CC.GRAY + "Rank: " + procedure.getRank().getDisplayName(),
                            CC.GRAY + "Duration: " + CC.WHITE + TimeUtil.getNormalDuration(procedure.getDuration()),
                            "",
                            CC.GREEN + "Click to select this reason!"
                    ).build()
            ).onClick(() -> {
                user.sendMessage(CC.PURPLE + "[Rank] " + CC.GRAY + CC.GRAY + "You have selected " + CC.GOLD + reason.getName() + CC.GRAY + " as the reason.");

                procedure.setReason(reason.getName());
                procedure.confirm();
            });
            if (index.get() == 13) index.getAndIncrement();
        });
        set(22, new ItemBuilder(Material.BOOK_AND_QUILL)
                .setName(CC.PINK + "Custom")
                .setLore("",
                        CC.GRAY + "Player: " + CC.WHITE + procedure.getTarget().getRealName(),
                        CC.GRAY + "Rank: " + procedure.getRank().getDisplayName(),
                        CC.GRAY + "Duration: " + CC.WHITE + TimeUtil.getNormalDuration(procedure.getDuration()),
                        "",
                        CC.GREEN + "Click to input a custom reason!"
                ).build()
        ).onClick(() -> {
            user.sendMessage('\n' + CC.PINK + "Please input a custom reason in chat.");
            user.sendMessage(CC.ITALIC_GRAY + "(Cancel the procedure by typing 'cancel')\n ");
            close(false);
        });
    }

    @Override
    public void onClose() {
        procedure.cancel();
    }
}
