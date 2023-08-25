package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.model.Channel;
import net.exemine.api.rank.Rank;
import net.exemine.api.redis.RedisService;
import net.exemine.api.util.StringUtil;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;

import java.util.List;

public class StaffChatCommand extends BaseCommand<CoreUser, CoreData> {

    private final RedisService redisService;

    public StaffChatCommand(RedisService redisService) {
        super(List.of("staffchat", "sc"), Rank.TRIAL_MOD);
        this.redisService = redisService;
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        Channel type = Channel.STAFF;

        if (args.length == 0) {
            user.toggleChannel(type);
            return;
        }
        String message = type.getChatFormat(InstanceUtil.getCurrent(), user.getColoredRealName(), StringUtil.join(args, 0));
        redisService.getPublisher().sendAlertStaffMessage(type.getRank(), message);
    }
}
