package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;

import java.util.List;

public class PracticeCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;
    private final UHCUserService userService;

    public PracticeCommand(GameService gameService, UHCUserService userService) {
        super(List.of("practice", "prac", "p"));
        this.gameService = gameService;
        this.userService = userService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (gameService.isNotState(GameState.LOBBY)) {
            user.sendMessage(CC.RED + "You cannot do that in the current game state.");
            return;
        }
        if (ToggleOption.PRACTICE.isDisabled()) {
            user.sendMessage(CC.RED + "Practice is not enabled.");
            return;
        }
        if (args.length > 0 && !user.isEqualOrAbove(Rank.ADMIN)) {
            user.performCommand("practice");
            return;
        }
        if (args.length == 0) {
            if (user.isSpectating()) {
                user.sendMessage(CC.RED + "You cannot do that in your current state.");
                return;
            }
            if (user.inState(UHCUserState.PRACTICE)) {
                userService.getPracticeUsers().forEach(practiceUser ->
                        practiceUser.sendMessage(CC.BOLD_GOLD + "[Practice] " + user.getColoredDisplayName() + CC.GRAY + " has left. " + CC.WHITE + '(' + (userService.getPracticeUsers().size() - 1) + '/' + NumberOption.PRACTICE_SLOTS.getValue() + ')')
                );
                user.setState(UHCUserState.LOBBY);
                return;
            }
            if (user.getData().hasNotSetupPracticeLayout()) {
                user.performCommand("layout");
                return;
            }
            user.sendMessage(CC.ITALIC_GRAY + "(Modify your practice layout with " + CC.BOLD + "/layout" + CC.ITALIC_GRAY + ')');
            user.setState(UHCUserState.PRACTICE);
            userService.getPracticeUsers().forEach(practiceUser ->
                    practiceUser.sendMessage(CC.BOLD_GOLD + "[Practice] " + user.getColoredDisplayName() + CC.GRAY + " has joined. " + CC.WHITE + '(' + userService.getPracticeUsers().size() + '/' + NumberOption.PRACTICE_SLOTS.getValue() + ')')
            );
            return;
        }
        if (args[0].equalsIgnoreCase("slots")) {
            if (args.length != 2 || !StringUtil.isInteger(args[1])) {
                user.sendMessage(CC.RED + "Usage: /practice slots <number>");
                return;
            }
            int slots = Integer.parseInt(args[1]);

            if (NumberOption.PRACTICE_SLOTS.hasValue(slots)) {
                user.sendMessage(CC.RED + "Practice slots are already set to " + CC.BOLD + slots + CC.RED + '.');
                return;
            }
            NumberOption.PRACTICE_SLOTS.setValue(slots);
            MessageUtil.send(CC.BOLD_GOLD + "[Practice] " + user.getColoredDisplayName() + CC.GRAY + " has updated practice slots to " + CC.WHITE + slots + CC.GRAY + '.');
        } else {
            user.sendMessage(CC.RED + "Usage: /practice [slots] [number]");
        }
    }
}
