package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.proxy.ProxyCheck;
import net.exemine.api.proxy.ProxyCheckState;
import net.exemine.api.proxy.ProxyService;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.Core;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.command.CommandSender;

import java.util.List;

public class VPNCommand extends BaseCommand<CoreUser, CoreData> {

    private final ProxyService proxyService;
    private final UserService<CoreUser, CoreData> userService;

    public VPNCommand(Core plugin) {
        super(List.of("vpn"), Rank.ADMIN, false);
        this.proxyService = plugin.getProxyService();
        this.userService = plugin.getUserService();
        setAsync(true);
        setUsage(CC.RED + "Usage: /vpn <player> <blacklisted|whitelisted|normal>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(getUsage());
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(
                target -> {
                    ProxyCheckState state = ProxyCheckState.get(args[1]);

                    if (state == null) {
                        sender.sendMessage(getUsage());
                        return;
                    }
                    ProxyCheck check = proxyService.getOrCheckAddress(target.getData().getAddress());
                    if (state == check.getState()) {
                        sender.sendMessage(target.getColoredRealName() + CC.RED + "'s VPN status is already set to " + CC.BOLD + state.getName() + CC.RED + '.');
                        return;
                    }
                    proxyService.updateCheck(check, state);
                    sender.sendMessage(CC.PURPLE + "[VPN] " + CC.GRAY + "You've updated " + target.getColoredRealName() + CC.GRAY + "'s VPN status to " + CC.GOLD + state.getName() + CC.GRAY + '.');
                },
                () -> sender.sendMessage(Lang.USER_NEVER_PLAYED));
    }
}
