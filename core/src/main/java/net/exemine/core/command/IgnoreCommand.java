package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IgnoreCommand extends BaseCommand<CoreUser, CoreData> {

    public IgnoreCommand() {
        super(List.of("ignore", "ignores"));
        setUsage(CC.RED + "Usage: /ignore <add|remove|clear|list> [player]");
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (args.length == 0 || args.length > 2) {
            user.sendMessage(getUsage());
            return;
        }
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "clear":
                    if (data.getIgnoreList().isEmpty()) {
                        user.sendMessage(CC.RED + "You are not ignoring anyone.");
                        return;
                    }
                    data.getIgnoreList().clear();

                    user.saveData(false);
                    user.sendMessage(CC.PURPLE + "[Ignore] " + CC.GRAY + "You have cleared the list.");
                    break;
                case "list":
                    if (data.getIgnoreList().isEmpty()) {
                        user.sendMessage(CC.RED + "You are not ignoring anyone.");
                        return;
                    }
                    user.sendMessage(CC.PURPLE + "[Ignore] " + CC.GRAY + "You are ignoring: " + data.getIgnoreList()
                            .stream()
                            .map(uuid -> userService.fetch(uuid))
                            .filter(Optional::isPresent)
                            .map(ignoredUser -> ignoredUser.get().getColoredDisplayName())
                            .collect(Collectors.joining(CC.GRAY + ", ")));
                    break;
                default:
                    user.sendMessage(getUsage());
            }
            return;
        }
        userService.fetch(args[1]).ifPresentOrElse(target -> {
            if (user == target) {
                user.sendMessage(CC.RED + "You cannot ignore yourself.");
                return;
            }
            switch (args[0].toLowerCase()) {
                case "add":
                    if (data.isIgnoring(target.getUniqueId())) {
                        user.sendMessage(CC.RED + "You are already ignoring " + target.getColoredDisplayName() + CC.RED + '.');
                        return;
                    }
                    data.getIgnoreList().add(target.getUniqueId());
                    user.saveData(false);
                    user.sendMessage(CC.PURPLE + "[Ignore] " + CC.GRAY + "You are now ignoring " + target.getColoredDisplayName() + CC.GRAY + '.');
                    break;
                case "remove":
                    if (!data.isIgnoring(target.getUniqueId())) {
                        user.sendMessage(CC.RED + "You are not ignoring " + target.getColoredDisplayName() + CC.RED + '.');
                        return;
                    }
                    data.getIgnoreList().remove(target.getUniqueId());
                    user.saveData(false);
                    user.sendMessage(CC.PURPLE + "[Ignore] " + CC.GRAY + "You are no longer ignoring " + target.getColoredDisplayName() + CC.GRAY + '.');
                    break;
                default:
                    user.sendMessage(getUsage());
            }
        }, () -> user.sendMessage(Lang.USER_NEVER_PLAYED));
    }
}
