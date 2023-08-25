package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.Instance;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.List;

public class GlobalListCommand extends BaseCommand<CoreUser, CoreData> {

    private final InstanceService instanceService;

    public GlobalListCommand(InstanceService instanceService) {
        super(List.of("globallist", "glist"), Rank.TRIAL_MOD, false);
        this.instanceService = instanceService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int globalCount = instanceService.getOnlinePlayers();
        sender.sendMessage("");
        sender.sendMessage(CC.PINK + "There " + (globalCount == 1 ? "is" : "are") + ' ' + CC.GOLD + globalCount + CC.PINK
                + " player" + StringUtil.getPlural(globalCount) + " connected to the network:");
        instanceService.getAllInstances()
                .stream()
                .sorted(Comparator.comparingInt(Instance::getOnlinePlayers).reversed())
                .forEach(instance -> sender.sendMessage(Lang.LIST_PREFIX + CC.PURPLE + instance.getName()
                        + CC.GRAY + " (" + CC.WHITE + instance.getOnlinePlayers() + '/' + instance.getMaxPlayers() + CC.GRAY + ')'));
        sender.sendMessage("");
    }
}
