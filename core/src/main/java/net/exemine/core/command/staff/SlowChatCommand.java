package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.model.Channel;
import net.exemine.api.rank.Rank;
import net.exemine.api.redis.RedisService;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.server.ServerService;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;

public class SlowChatCommand extends BaseCommand<CoreUser, CoreData> {

    private final ServerService serverService;
    private final RedisService redisService;

    private final long offValue = 0L;

    public SlowChatCommand(ServerService serverService, RedisService redisService) {
        super(List.of("slowchat", "chatdelay", "delaychat"), Rank.SENIOR_MOD, false);
        this.serverService = serverService;
        this.redisService = redisService;
        setUsage(CC.RED + "Usage: /slowchat <time|off> [seconds]");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(getUsage());
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("off")) {
            disableChatDelay(sender);
            return;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("time") && StringUtil.isInteger(args[1])) {
            int seconds = Integer.parseInt(args[1]);

            if (seconds <= 0) {
                disableChatDelay(sender);
                return;
            }
            if (seconds > 60) {
                sender.sendMessage(CC.RED + "Please input a number between 1 and 60.");
                return;
            }
            long millis = seconds * 1000L;

            if (serverService.getChatDelay() == millis) {
                sender.sendMessage(CC.RED + "Chat delay is already set to " + CC.BOLD + seconds + CC.RED + " seconds.");
                return;
            }
            serverService.setChatDelay(millis);
            notifyOnlineStaff(sender, millis);
            return;
        }
        sender.sendMessage(getUsage());
    }

    private void disableChatDelay(CommandSender sender) {
        if (serverService.getChatDelay() == offValue) {
            sender.sendMessage(CC.RED + "Chat delay is already turned off.");
            return;
        }
        serverService.setChatDelay(offValue);
        notifyOnlineStaff(sender, offValue);
    }

    private void notifyOnlineStaff(CommandSender sender, long millis) {
        String name = sender instanceof ConsoleCommandSender
                ? Lang.CONSOLE_FORMAT
                : userService.get(sender).getColoredRealName();
        Channel channel = Channel.STAFF;
        String message = channel.format(name, CC.GRAY + ' ' + (millis == offValue
                ? "disabled the chat delay"
                : "updated the chat delay to " + channel.getInfoColor() + (millis / 1000L) + CC.GRAY + " seconds")
                + " on " + channel.getInfoColor() + InstanceUtil.getName() + channel.getFormatColor() + '.');

        redisService.getPublisher().sendAlertStaffMessage(channel.getRank(), message);
    }
}
