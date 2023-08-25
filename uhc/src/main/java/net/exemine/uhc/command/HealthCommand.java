package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserState;

import java.util.List;

public class HealthCommand extends BaseCommand<UHCUser, UHCData> {

    public HealthCommand() {
        super(List.of("health", "h", "hp"));
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (args.length > 1) {
            user.sendMessage(CC.RED + "Usage: /health <player>");
            return;
        }
        UHCUser target = args.length == 0 ? user : userService.get(args[0]);
        if (isUserOffline(user, target)) return;

        if (target.notInState(UHCUserState.PRACTICE, UHCUserState.IN_GAME)) {
            user.sendMessage(CC.RED + "That player is currently not in a PvP state.");
            return;
        }
        float absorption = target.getAbsorption();
        user.sendMessage(CC.PURPLE + "[Health] " + (user == target ? CC.GRAY + "You're" : target.getColoredDisplayName() + CC.GRAY + " is")
                + " currently at " + CC.RED + target.getFormattedHealth() + CC.DARK_RED + Lang.HEART
                + (absorption > 0f ? CC.GRAY + " (" + CC.YELLOW + target.getFormattedAbsorption() + CC.GOLD + Lang.HEART + CC.GRAY + ')' : "") + CC.GRAY + '.');
    }
}
