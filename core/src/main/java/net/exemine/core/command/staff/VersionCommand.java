package net.exemine.core.command.staff;

import com.execets.spigot.protocol.ProtocolVersion;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class VersionCommand extends BaseCommand<CoreUser, CoreData> {

    public VersionCommand() {
        super(List.of("version", "ver", "protocol"), Rank.TRIAL_MOD, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(CC.RED + "Usage: /version [player]");
            return;
        }
        if (args.length == 0) {
            sender.sendMessage("");
            sender.sendMessage(CC.PINK + "Player counts depending on client versions:");
            Arrays.stream(ProtocolVersion.values()).forEach(protocolVersion -> {
                int count = (int) userService.getOnlineUsers()
                        .stream()
                        .filter(online -> online.getProtocolVersion() == protocolVersion)
                        .count();
                if (count == 0) return;
                sender.sendMessage(Lang.LIST_PREFIX + CC.PURPLE + protocolVersion.getName() + CC.GRAY + " (" + CC.WHITE + count + CC.GRAY + ')');
            });
            sender.sendMessage("");
            return;
        }
        CoreUser target = userService.get(args[0]);
        if (isUserOffline(sender, target)) return;
        sender.sendMessage(CC.PURPLE + "[Version] " + target.getColoredDisplayName() + CC.GRAY + " is playing on "
                + CC.GOLD + target.getProtocolVersion().getName() + CC.GRAY + '.');
    }
}
