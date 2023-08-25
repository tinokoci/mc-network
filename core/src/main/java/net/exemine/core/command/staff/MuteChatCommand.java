package net.exemine.core.command.staff;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.server.ServerService;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MuteChatCommand extends BaseCommand<CoreUser, CoreData> {

    private final ServerService serverService;

    public MuteChatCommand(ServerService serverService) {
        super(List.of("mutechat"), InstanceUtil.isType(InstanceType.UHC) ? Rank.TRIAL_MOD : Rank.SENIOR_MOD, false);
        this.serverService = serverService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        serverService.setChatMuted(!serverService.isChatMuted());

        String message = serverService.isChatMuted()
                ? CC.RED + "Public chat is now muted."
                : CC.GREEN + "Public chat is no longer muted.";
        Bukkit.broadcastMessage(message);
    }
}