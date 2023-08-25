package net.exemine.core.profile;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;
import java.util.Optional;

public class ProfileCommand extends BaseCommand<CoreUser, CoreData> {

    public ProfileCommand() {
        super(List.of("profile"));
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        Optional<CoreUser> targetOptional = args.length == 0
                ? Optional.of(user)
                : userService.fetch(args[0]);

        if (targetOptional.isEmpty()) {
            user.sendMessage(Lang.USER_NEVER_PLAYED);
            return;
        }
        new ProfileViewMenu(user, targetOptional.get()).open();
    }
}
