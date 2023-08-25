package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.server.ServerService;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.ExeUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand extends BaseCommand<CoreUser, CoreData> {

    private final ServerService serverService;

    public ListCommand(ServerService serverService) {
        super(List.of("list"));
        this.serverService = serverService;

        serverService.setListCallback(() -> {
            List<String> list = new ArrayList<>();
            // Staff
            List<CoreUser> staff = userService.getOnlineUsers()
                    .stream()
                    .filter(user -> user.isEqualOrAbove(RankType.STAFF) && !user.isDisguised())
                    .sorted(Comparator.comparing(online -> online.getRank().getPriority()))
                    .collect(Collectors.toList());
            if (!staff.isEmpty()) {
                list.add(CC.GRAY + "Staff " + '(' + staff.size() + "): " + staff
                        .stream()
                        .map(CoreUser::getColoredRealName)
                        .collect(Collectors.joining(CC.GRAY + ", ")));
            }
            // Others
            Comparator<CoreUser> byDisguise = Comparator.comparing(ExeUser::isDisguised);
            Comparator<CoreUser> byPriority = Comparator.comparing(user -> user.getRank().getPriority());

            List<CoreUser> others = userService.getOnlineUsers()
                    .stream()
                    .filter(user -> !user.isEqualOrAbove(RankType.STAFF) || user.isDisguised())
                    .sorted(byDisguise.thenComparing(byPriority))
                    .collect(Collectors.toList());
            if (!others.isEmpty()) {
                list.add(CC.GRAY + "Online " + '(' + others.size() + "): " + others
                        .stream()
                        .map(CoreUser::getColoredDisplayName)
                        .collect(Collectors.joining(CC.GRAY + ", ")));
            }
            return list;
        });
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        user.sendMessage(Arrays.stream(Rank.values())
                .map(Rank::getDisplayName)
                .collect(Collectors.joining(CC.GRAY + ", "))
        );
        serverService.getListCallback().get().forEach(user::sendMessage);
    }
}
