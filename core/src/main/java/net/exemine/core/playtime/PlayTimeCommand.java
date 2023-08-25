package net.exemine.core.playtime;

import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;

import java.util.List;

public class PlayTimeCommand extends BaseCommand<CoreUser, CoreData> {

    private final BulkDataService bulkDataService;
    private final UserService<CoreUser, CoreData> userService;

    public PlayTimeCommand(BulkDataService bulkDataService, UserService<CoreUser, CoreData> userService) {
        super(List.of("playtime"));
        this.userService = userService;
        this.bulkDataService = bulkDataService;
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        CoreUser target = args.length == 0
                ? user
                : userService.fetch(args[0]).orElse(null);

        if (target == null) {
            assert user != null;
            user.sendMessage(Lang.USER_NEVER_PLAYED);
            return;
        }
        bulkDataService.loadPlayTimes(target.getBulkData());
        showPlayTime(user, target);
    }

    private void showPlayTime(CoreUser user, CoreUser target) {
        if (user.isEqualOrAbove(Rank.MANAGER)) {
            new PlaytimeViewMenu(user, target).open();
        } else {
            showNormalPlayTime(user, target);
        }
    }

    private void showNormalPlayTime(CoreUser user, CoreUser target) {
        String prefix = user == target ? CC.GRAY + "Your" : target.getColoredDisplayName() + "'s";
        long playtime = target.getBulkData().getPlayTime(target.getSessionLoginTime());
        user.sendMessage(CC.PURPLE + "[Playtime] " + prefix + CC.GRAY + " total: " + CC.GOLD + TimeUtil.getFullDuration(playtime));
    }
}
