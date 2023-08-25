package net.exemine.core.rank.procedure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import net.exemine.core.menu.confirm.ConfirmMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.ExeUser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
public class RankProcedure {

    private static final Map<UUID, RankProcedure> PROCEDURES = new HashMap<>();

    private final CoreUser user;

    private CoreUser target;
    private Rank rank;
    private long duration;
    private String reason;
    private boolean add;

    private RankProcedureState state = RankProcedureState.RANK;

    public RankProcedure(CoreUser user, CoreUser target, boolean add) {
        this.user = user;
        this.target = target;
        this.add = add;

        PROCEDURES.put(user.getUniqueId(), this);
    }

    public RankProcedure setTarget(CoreUser target) {
        this.target = target;
        return this;
    }

    public static RankProcedure getProcedure(ExeUser<?> user) {
        return PROCEDURES.getOrDefault(user.getUniqueId(), null);
    }

    public void confirm() {
        new ConfirmMenu(user, true, confirm -> {
            cancel();
            if (!confirm) return;

            Executor.schedule(() -> {
                String previousRealName = target.getColoredRealName();
                boolean hasMainRankChanged = Core.get().getRankService().addRank(target.getData(), target.getBulkData(), rank, user.getUniqueId(), duration, reason);

                if (hasMainRankChanged) {
                    user.sendMessage(CC.PURPLE + "[Rank] " + CC.GRAY + "You have updated " + previousRealName + CC.GRAY + "'s rank to " + rank.getDisplayName() + CC.GRAY + '.');
                } else {
                    user.sendMessage(CC.PURPLE + "[Rank] " + CC.GRAY + "You have added " + rank.getDisplayName() + CC.GRAY + " to " + previousRealName + CC.GRAY + '.');
                }
            }).runAsync();
        }).open();
    }

    public void cancel() {
        PROCEDURES.remove(user.getUniqueId());
    }
}