package net.exemine.core.disguise.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;
import java.util.stream.Collectors;

public class DisguiseListCommand extends BaseCommand<CoreUser, CoreData> {

    public DisguiseListCommand() {
        super(List.of("disguiselist", "dlist"), Rank.TRIAL_MOD);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        List<CoreUser> disguisedUsers = userService.getOnlineUsers()
                .stream()
                .filter(CoreUser::isDisguised)
                .collect(Collectors.toList());

        if (disguisedUsers.isEmpty()) {
            user.sendMessage(CC.RED + "There are no disguised players on this server.");
            return;
        }
        user.sendMessage("");
        user.sendMessage(CC.PINK + "Lists of disguised players:");
        disguisedUsers.forEach(disguisedUser ->
                user.sendMessage(Lang.LIST_PREFIX + CC.WHITE + disguisedUser.getBulkData().getDisguiseModel().getName()
                        + CC.GRAY + " (" + disguisedUser.getColoredRealName() + CC.GRAY + ')')
        );
        user.sendMessage("");
    }
}
