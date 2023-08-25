package net.exemine.core.command.staff;

import java.util.List;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.server.ServerService;
import net.exemine.core.server.task.RebootTask;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.MessageUtil;
import org.bukkit.command.CommandSender;

public class RebootCommand extends BaseCommand<CoreUser, CoreData> {

    private final Core plugin;

    public RebootCommand(Core plugin) {
        super(List.of("reboot", "shutdown", "stop", "restart"), Rank.DEVELOPER, false);
        this.plugin = plugin;
        setUsage(CC.RED + "Usage: /reboot <time|cancel>");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(getUsage());
            return;
        }
        ServerService serverService = plugin.getServerService();
        RebootTask rebootTask = serverService.getRebootTask();

        if (args[0].equalsIgnoreCase("cancel")) {
            if (rebootTask == null) {
                sender.sendMessage(CC.RED + "This instance is not scheduled to restart.");
                return;
            }
            rebootTask.cancel();
            serverService.setRebootTask(null);
            MessageUtil.send(CC.RED + "The automatic server restart was cancelled.");
            return;
        }
        if (rebootTask != null) {
            sender.sendMessage(CC.RED + "This instance is already scheduled to restart.");
            return;
        }
        long millis = TimeUtil.getMillisFromInput(args[0]);

        if (millis == Long.MAX_VALUE) {
            sender.sendMessage(getUsage());
            return;
        }
        serverService.setRebootTask(new RebootTask(plugin, millis));
    }
}
