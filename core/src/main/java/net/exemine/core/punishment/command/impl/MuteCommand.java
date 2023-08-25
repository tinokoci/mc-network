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

public class MuteCommand extends PunishmentCommand {

    public MuteCommand(PunishmentService punishmentService, UserService<CoreUser, CoreData> userService) {
        super(List.of("mute", "tempmute"), Rank.TRIAL_MOD, punishmentService, userService);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player && openMenu(sender, args, PunishmentType.MUTE)) return;

        punish(sender, args, PunishmentType.MUTE, true, PunishmentType.MUTE);
    }
}
