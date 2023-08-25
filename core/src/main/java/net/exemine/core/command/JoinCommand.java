package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.Instance;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;

import java.util.List;
import java.util.stream.Collectors;

public class JoinCommand extends BaseCommand<CoreUser, CoreData> {

    private final InstanceService instanceService;

    public JoinCommand(InstanceService instanceService) {
        super(List.of("join", "server"));
        this.instanceService = instanceService;
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length != 1) {
            user.sendMessage(CC.RED + "Usage: /join <server>");
            user.sendMessage(CC.RED + "Available servers: " + instanceService.getAllInstances()
                    .stream()
                    .filter(instance -> !InstanceUtil.isBlockedByWhitelist(user, instance))
                    .map(Instance::getName)
                    .collect(Collectors.joining(", ")));
            return;
        }
        String instanceName = args[0];
        Instance instance = instanceService.getInstance(instanceName);

        if (instance.isOffline()) {
            user.sendMessage(CC.RED + "Instance with the name '" + instanceName + "' cannot be found.");
            return;
        }
        if (instance.equals(InstanceUtil.getCurrent())) {
            user.sendMessage(CC.RED + "You're already connected to the '" + instance.getName() + "' instance.");
            return;
        }
        if (instance.isFull() && !user.isEqualOrAbove(RankType.DONATOR)) {
            user.sendMessage(CC.RED + "Instance with the name '" + instanceName + "' is full.");
            user.sendMessage(CC.RED + "You can bypass this by purchasing a rank @ " + CC.UNDERLINE + Lang.STORE);
            return;
        }
        if (instance.isWhitelisted() && !InstanceUtil.canJoin(user, instance)) {
            if (user.isEqualOrAbove(RankType.STAFF)) {
                user.sendMessage(CC.RED + "Instance with the name '" + instanceName + "' is limited to " + CC.BOLD + instance.getWhitelistRank().getName() + CC.RED + " and above.");
            } else {
                user.sendMessage(CC.RED + "Instance with the name '" + instanceName + "' is whitelisted.");
            }
            return;
        }
        user.sendToInstance(instance);
    }
}
