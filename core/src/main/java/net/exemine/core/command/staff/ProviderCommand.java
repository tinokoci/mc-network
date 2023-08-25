package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.provider.nametag.NametagService;
import net.exemine.core.provider.scoreboard.ScoreboardService;
import net.exemine.core.user.CoreUser;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ProviderCommand extends BaseCommand<CoreUser, CoreData> {

    public ProviderCommand() {
        super(List.of("adapter"), Rank.ADMIN, false);
        setUsage(CC.RED + "Usage: /adapter <scoreboard|tablist|nametags> [interval]");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(getUsage());
            return;
        }
        switch (args[0].toLowerCase()) {
            case "scoreboard":
                ScoreboardService<?> scoreboard = ScoreboardService.get();

                if (scoreboard == null) {
                    sender.sendMessage(CC.RED + "This server doesn't have a scoreboard implementation.");
                    return;
                }
                if (args.length == 1) {
                    sender.sendMessage(CC.PURPLE + "[Provider] " + CC.GRAY + "Interval of " + CC.GOLD + "scoreboard" + CC.GRAY
                            + " updates is set to " + CC.GOLD + scoreboard.getIntervalInMillis() + CC.GRAY + " milliseconds.");
                    return;
                }
                long interval = getInterval(sender, args[1]);
                if (interval == -1L) return;

                scoreboard.reschedule(interval);
                sender.sendMessage(CC.PURPLE + "[Provider] " + CC.GRAY + "You've updated the interval of " + CC.GOLD + "scoreboard" + CC.GRAY
                        + " updates to " + CC.GOLD + interval + CC.GRAY + " milliseconds.");
                break;
            case "tablist":
                sender.sendMessage("Tablist is not implemented yet.");
                break;
            case "nametags":
                NametagService<?> nametagService = NametagService.get();

                if (nametagService == null) {
                    sender.sendMessage(CC.RED + "This server doesn't have a nametags implementation.");
                    return;
                }
                if (args.length == 1) {
                    sender.sendMessage(CC.PURPLE + "[Provider] " + CC.GRAY + "Interval of " + CC.GOLD + "nametags" + CC.GRAY
                            + " updates is set to " + CC.GOLD + nametagService.getIntervalInMillis() + CC.GRAY + " milliseconds.");
                    return;
                }
                interval = getInterval(sender, args[1]);
                if (interval == -1L) return;

                nametagService.reschedule(interval);
                sender.sendMessage(CC.PURPLE + "[Provider] " + CC.GRAY + "You've updated the interval of " + CC.GOLD + "nametags" + CC.GRAY
                        + " updates to " + CC.GOLD + interval + CC.GRAY + " milliseconds.");
                break;
            default:
                sender.sendMessage(getUsage());
        }
    }

    private long getInterval(CommandSender sender, String input) {
        if (!StringUtil.isInteger(input)) {
            sender.sendMessage(getUsage());
            return -1L;
        }
        long interval = Long.parseLong(input);

        if (interval < 100 || interval > 10000) {
            sender.sendMessage(CC.RED + "Interval must be in the [100, 10000] milliseconds range.");
            return -1L;
        }
        return interval;
    }
}
