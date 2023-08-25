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

public class KickCommand extends PunishmentCommand {

    public KickCommand(PunishmentService punishmentService, UserService<CoreUser, CoreData> userService) {
        super(List.of("kick"), Rank.TRIAL_MOD, punishmentService, userService);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        punish(sender, args, PunishmentType.KICK, false, PunishmentType.KICK);
    }
}
