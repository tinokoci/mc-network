package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.Cooldown;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;

import java.util.List;
import java.util.UUID;

public class HelpOpCommand extends BaseCommand<UHCUser, UHCData> {

    private final UHCUserService userService;
    private final Cooldown<UUID> cooldown = new Cooldown<>();

    public HelpOpCommand(UHCUserService userService) {
        super(List.of("helpop"));
        this.userService = userService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (cooldown.isActive(user.getUniqueId())) {
            user.sendMessage(CC.RED + "Please wait " + CC.BOLD + cooldown.getNormalDuration(user.getUniqueId()) + CC.RED + " before using that again.");
            return;
        }
        if (args.length == 0) {
            user.sendMessage(CC.RED + "Usage: /helpop <message>");
            return;
        }
        int cooldownSeconds = user.getBulkData().hasActivePunishment(PunishmentType.MUTE) ? 60 * 7 : user.isEqualOrAbove(RankType.DONATOR) ? 30 : 60;
        cooldown.put(user.getUniqueId(), cooldownSeconds);

        String message = CC.DARK_RED + "[HelpOp] " +  user.getColoredDisplayName() + CC.RED + " requested assistance: " + CC.GRAY + StringUtil.join(args);
        userService.getModAndHostUsers()
                .stream()
                .filter(modOrHost -> modOrHost.getStaffData().isHelpOpAlerts())
                .forEach(modOrHost -> modOrHost.sendMessage(message));
        user.sendMessage(CC.GREEN + "You've sent a message to the game moderators.");

    }
}
