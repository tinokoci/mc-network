package net.exemine.core.command.toggle;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class ToggleParticlesCommand extends BaseCommand<CoreUser, CoreData> {

    public ToggleParticlesCommand() {
        super(List.of("toggleparticles", "particles", "particle"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        data.setParticles(!data.isParticles());
        user.saveData(true);
        user.sendMessage(CC.PURPLE + "[Settings] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(data.isParticles()) + CC.GRAY + " see particle effects.");
    }
}

