package net.exemine.core.util;

import net.exemine.api.instance.Instance;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.RankType;
import net.exemine.core.Core;
import net.exemine.core.user.base.ExeUser;

public class InstanceUtil {

    // Cannot be used on initialization because instance is defined last on startup hook
    public static Instance getCurrent() {
        return Core.get().getInstanceService().getCurrentInstance();
    }

    public static String getName() {
        return Core.get().getInstanceName();
    }

    public static InstanceType getType() {
        return Core.get().getInstanceType();
    }

    public static boolean isType(InstanceType type) {
        return getType() == type;
    }

    public static boolean isFull(ExeUser<?> user, Instance instance) {
        return instance.isFull() && !user.isEqualOrAbove(RankType.DONATOR);
    }

    public static boolean isBlockedByWhitelist(ExeUser<?> user, Instance instance) {
        return instance.isWhitelisted() && !user.isEqualOrAbove(instance.getWhitelistRank());
    }

    public static boolean canJoin(ExeUser<?> user, Instance instance) {
        return !instance.isOffline() && !isFull(user, instance) && !isBlockedByWhitelist(user, instance);
    }
}
