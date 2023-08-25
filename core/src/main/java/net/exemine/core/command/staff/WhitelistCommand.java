package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.server.ServerService;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WhitelistCommand extends BaseCommand<CoreUser, CoreData> {

    private final ServerService serverService;

    public WhitelistCommand(ServerService serverService) {
        super(InstanceUtil.isType(InstanceType.UHC) ? List.of("corewhitelist", "corewl") : List.of("whitelist", "wl"), Rank.DEVELOPER, false);
        this.serverService = serverService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendUsage(sender);
            return;
        }
        Rank whitelistRank = Rank.get(args[0]);

        if (whitelistRank == null) {
            sendUsage(sender);
            return;
        }
        if (sender instanceof Player && !userService.get(sender).isEqualOrAbove(whitelistRank)) {
            sender.sendMessage(CC.RED + "You cannot set that whitelist rank because you don't have a high enough rank.");
            return;
        }
        if (InstanceUtil.getCurrent().getWhitelistRank().isEqual(whitelistRank)) {
            sender.sendMessage(CC.RED + "This instance already has " + CC.BOLD + whitelistRank.getName() + CC.RED + " whitelist.");
            return;
        }
        serverService.updateWhitelist(whitelistRank);
        sender.sendMessage(CC.PURPLE + "[Whitelist] " + CC.GRAY + "Only " + CC.GOLD + whitelistRank.getDisplayName() + CC.GRAY + " and higher ranked players can now join this instance.");
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(CC.RED + "Usage: /whitelist <type>");
        sender.sendMessage(CC.RED + "Current: " + CC.BOLD + InstanceUtil.getCurrent().getWhitelistRank().getName());
        sender.sendMessage(CC.RED + "Types: " + Arrays.stream(Rank.values())
                .map(Rank::name)
                .collect(Collectors.joining(", ")));
    }
}
