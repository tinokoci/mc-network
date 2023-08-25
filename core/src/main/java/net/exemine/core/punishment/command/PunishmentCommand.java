package net.exemine.core.punishment.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.punishment.Punishment;
import net.exemine.api.punishment.PunishmentService;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.punishment.event.PunishmentEvent;
import net.exemine.core.punishment.menu.selector.BanMenu;
import net.exemine.core.punishment.menu.selector.MuteMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class PunishmentCommand extends BaseCommand<CoreUser, CoreData> {

    private final PunishmentService punishmentService;
    private final UserService<CoreUser, CoreData> userService;

    public PunishmentCommand(List<String> aliases, Rank rank, PunishmentService punishmentService, UserService<CoreUser, CoreData> userService) {
        super(aliases, rank, false);
        this.punishmentService = punishmentService;
        this.userService = userService;
        setAsync(true);
    }

    public void punish(CommandSender sender, String[] args, PunishmentType type, boolean hasDuration, PunishmentType... allTypes) {
        if (args.length < 2) {
            sender.sendMessage(CC.RED + "Usage: /" + type.getName().toLowerCase().replace(" ", "") + " <player> " + (hasDuration ? "<duration> " : "") + "<reason>");
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(target -> {
            if (type == PunishmentType.KICK && target.isOffline()) {
                sender.sendMessage(Lang.USER_NOT_FOUND);
                return;
            }
            if (sender instanceof Player && target.getRealName().equals(sender.getName())) {
                sender.sendMessage(CC.RED + "You cannot punish yourself.");
                return;
            }

            if (sender instanceof Player && !userService.get(sender).isAbove(target)) {
                sender.sendMessage(CC.RED + "You cannot punish " + target.getColoredRealName() + CC.RED + '.');
                return;
            }
            Punishment punishment = target.getBulkData().getActivePunishment(allTypes);

            if (punishment != null) {
                sender.sendMessage(target.getColoredRealName() + CC.RED + " is already " + punishment.getType().getFormat() + '.');
                return;
            }
            long duration = !hasDuration ? Long.MAX_VALUE : TimeUtil.getMillisFromInput(args[1]);
            String reason = StringUtil.join(args, duration == Long.MAX_VALUE ? 1 : 2);
            if (reason.isEmpty()) reason = "Unknown";

            punishment = punishmentService.addPunishment(target.getBulkData(), type, UserUtil.getJsonUUID(sender), duration, reason);
            Punishment finalPunishment = punishment;

            boolean temporary = !punishment.isPermanent();
            String message = (punishment.isAddedSilently() ? CC.GRAY + "(Silent) " : "")
                    + CC.RED + target.getRealName() + CC.RED + " has been "
                    + (punishment.getType() != PunishmentType.KICK ? (temporary ? "temporarily " : "permanently ") : "")
                    + punishment.getType().getFormat() + " for " + reason
                    + (temporary ? " for a duration of " + TimeUtil.getNormalDuration(duration) : "") + '.';
            userService.getOnlineUsers()
                    .stream()
                    .filter(user -> !finalPunishment.isAddedSilently() || user.isEqualOrAbove(RankType.STAFF))
                    .forEach(user -> user.sendMessage(message));

            Bukkit.getPluginManager().callEvent(new PunishmentEvent(punishment));
        }, () -> sender.sendMessage(Lang.USER_NEVER_PLAYED));
    }

    public void pardon(CommandSender sender, String[] args, PunishmentType... types) {
        if (args.length < 2) {
            sender.sendMessage(CC.RED + "Usage: /un" + types[0].getName().toLowerCase().replace(" ", "") + " <player> <reason>");
            return;
        }
        userService.fetch(args[0]).ifPresentOrElse(target -> {
            Punishment punishment = target.getBulkData().getActivePunishment(types);

            if (punishment == null) {
                sender.sendMessage(target.getColoredRealName() + CC.RED + " is not " + types[0].getFormat() + ".");
                return;
            }
            String reason = StringUtils.join(args, " ", 1, args.length);
            punishmentService.removePunishment(punishment, UserUtil.getJsonUUID(sender), reason);
            sender.sendMessage(CC.RED + "You have " + punishment.getType().getPardon() + ' ' + target.getRealName() + '.');
        }, () -> sender.sendMessage(Lang.USER_NEVER_PLAYED));
    }

    public boolean openMenu(CommandSender sender, String[] args, PunishmentType type) {
        CoreUser user = userService.get(sender);

        if (args.length == 1) {
            Optional<CoreUser> target = userService.fetch(args[0]);
            if (target.isEmpty()) return false;

            if (type == PunishmentType.BAN) {
                new BanMenu(user, target.get()).open();
                return true;
            } else if (type == PunishmentType.MUTE) {
                new MuteMenu(user, target.get()).open();
                return true;
            }
        }
        return false;
    }
}
