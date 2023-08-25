package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.practice.layout.PracticeLayoutSetupMenu;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserState;
import org.bukkit.Sound;

import java.util.List;

public class PracticeLayoutCommand extends BaseCommand<UHCUser, UHCData> {

    public PracticeLayoutCommand() {
        super(List.of("practicelayout", "editlayout", "layout"));
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (user.notInState(UHCUserState.LOBBY)) {
            user.sendMessage(CC.RED + "You cannot do that in your current state.");
            return;
        }
        if (user.getData().hasNotSetupPracticeLayout()) {
            user.playSound(Sound.SUCCESSFUL_HIT);
        }
        new PracticeLayoutSetupMenu(user).open();
    }
}
