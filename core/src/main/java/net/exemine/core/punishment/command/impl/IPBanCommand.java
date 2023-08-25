package net.exemine.core.punishment.command.impl;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.punishment.PunishmentService;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.rank.Rank;
import net.exemine.core.punishment.command.PunishmentCommand;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.command.CommandSender;

import java.util.List;

public class IPBanCommand extends PunishmentCommand {

    public IPBanCommand(PunishmentService punishmentService, UserService<CoreUser, CoreData> userService) {
        super(List.of("ipban", "banip", "tempipban", "tempbanip"), Rank.SENIOR_MOD, punishmentService, userService);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        punish(sender, args, PunishmentType.IP_BAN, true, PunishmentType.IP_BAN, PunishmentType.BAN);
    }
}
