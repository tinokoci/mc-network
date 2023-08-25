package net.exemine.core.punishment.command.impl;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.punishment.PunishmentService;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.rank.Rank;
import net.exemine.core.punishment.command.PunishmentCommand;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BanCommand extends PunishmentCommand {

    public BanCommand(PunishmentService punishmentService, UserService<CoreUser, CoreData> userService) {
        super(List.of("ban", "tempban"), Rank.MOD, punishmentService, userService);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player && openMenu(sender, args, PunishmentType.BAN)) return;

        punish(sender, args, PunishmentType.BAN, true, PunishmentType.IP_BAN, PunishmentType.BAN);
    }
}
