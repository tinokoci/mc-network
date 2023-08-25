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

public class UnmuteCommand extends PunishmentCommand {

    public UnmuteCommand(PunishmentService punishmentService, UserService<CoreUser, CoreData> userService) {
        super(List.of("unmute"), Rank.MOD, punishmentService, userService);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        pardon(sender, args, PunishmentType.MUTE);
    }
}
