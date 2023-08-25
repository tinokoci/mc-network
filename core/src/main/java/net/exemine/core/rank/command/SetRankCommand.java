package net.exemine.core.rank.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankService;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.rank.menu.RankSelectMenu;
import net.exemine.core.rank.procedure.RankProcedure;
import net.exemine.core.user.CoreUser;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetRankCommand extends BaseCommand<CoreUser, CoreData> {

    private final RankService rankService;

    public SetRankCommand(RankService rankService) {
        super(List.of("setrank", "grant"), Rank.MANAGER, false);
        this.rankService = rankService;
        setAsync(true);
    }

    @Override // setrank strongtino owner perm test
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            if (args.length < 4) {
                sender.sendMessage(CC.RED + "Usage: /setrank <player> [duration] [reason]");
                return;
            }
            userService.fetch(args[0]).ifPresentOrElse(target -> {
                Rank rank = Rank.get(args[1]);

                if (rank == null) {
                    sender.sendMessage(CC.RED + "Available ranks: " + StringUtil.enumToString(Arrays.stream(Rank.values()).filter(r -> r != Rank.DEFAULT).collect(Collectors.toList())));
                    return;
                }
                long duration = TimeUtil.getMillisFromInput(args[2]);
                String reason = StringUtil.join(args, 3);

                String previousRealName = target.getColoredRealName();
                boolean hasMainRankChanged = rankService.addRank(target.getData(), target.getBulkData(), rank, null, duration, reason);

                if (hasMainRankChanged) {
                    sender.sendMessage(CC.PURPLE + "[Rank] " + CC.GRAY + "You have updated " + previousRealName + CC.GRAY + "'s rank to " + rank.getDisplayName() + CC.GRAY + '.');
                } else {
                    sender.sendMessage(CC.PURPLE + "[Rank] " + CC.GRAY + "You have added " + rank.getDisplayName() + CC.GRAY + " to " + previousRealName + CC.GRAY + '.');
                }
            }, () -> sender.sendMessage(Lang.USER_NEVER_PLAYED));
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(CC.RED + "Usage: /setrank <player>");
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(
                target -> {
                    CoreUser user = userService.get(sender);
                    new RankProcedure(user, target, true);
                    new RankSelectMenu(user).open();
                },
                () -> sender.sendMessage(Lang.USER_NEVER_PLAYED)
        );
    }
}
