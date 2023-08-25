package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TopKillsCommand extends BaseCommand<UHCUser, UHCData> {

    private final GameService gameService;
    private final UHCUserService userService;

    public TopKillsCommand(GameService gameService, UHCUserService userService) {
        super(List.of("topkills", "killstop", "killtop", "kt", "tk"));
        this.gameService = gameService;
        this.userService = userService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (!gameService.isStateOrHigher(GameState.PLAYING)) {
            user.sendMessage(CC.RED + "You cannot do that in the current game state.");
            return;
        }
        user.sendMessage(CC.STRIKETHROUGH_GRAY + "---------------------------------");
        user.sendMessage("       " + CC.BOLD_PINK + "Top 10 Kills " + CC.GRAY + "(Current Game)");
        user.sendMessage();

        List<UHCUser> users = userService.values()
                .stream()
                .sorted(Comparator.comparing(u -> u.getGameInfo().getKills().getValue(), Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());
        for (int i = 0; i < 10; i++) {
            int position = i + 1;
            try {
                UHCUser target = users.get(i);
                user.sendMessage(' ' + CC.GRAY + '#' + position + ' ' + (target.isPlaying() ? CC.GREEN : CC.RED) +
                        target.getDisplayName() + CC.GRAY + ": " + CC.WHITE + target.getGameInfo().getKills().getValue());
            } catch (IndexOutOfBoundsException e) {
                user.sendMessage(' ' + CC.GRAY + '#' + position + ' ' + CC.GOLD + "Unknown" + CC.GRAY + ": " + CC.WHITE + "0");
            }
        }
        user.sendMessage(CC.STRIKETHROUGH_GRAY + "---------------------------------");
    }
}
