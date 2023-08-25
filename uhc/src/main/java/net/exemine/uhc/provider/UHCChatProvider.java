package net.exemine.uhc.provider;

import lombok.RequiredArgsConstructor;
import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.provider.ChatProvider;
import net.exemine.uhc.config.editor.OptionEditor;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@RequiredArgsConstructor
public class UHCChatProvider implements ChatProvider<UHCUser, UHCData> {

    private final GameService gameService;
    private final UHCUserService userService;

    @Override
    public void sendMessage(UHCUser user, UHCData data, AsyncPlayerChatEvent event) {
        NumberOption option = OptionEditor.get(user);
        if (user.isEqualOrAbove(RankType.STAFF) && option != null) {
            String[] args = event.getMessage().split(" ");
            if (args[0].equalsIgnoreCase("cancel")) {
                OptionEditor.remove(user);
                user.sendMessage(CC.RED + "You've cancelled the option modification process.");
                return;
            }
            if (args.length > 1 || !StringUtil.isInteger(args[0])) {
                user.sendMessage();
                user.sendMessage(CC.RED + "Please input a new value for the " + CC.BOLD + option.getName() + CC.RED + " option.");
                user.sendMessage(CC.ITALIC_GRAY + "(You can type 'cancel' to cancel the process.)");
                user.sendMessage();
                return;
            }
            int value = Integer.parseInt(args[0]);
            if (option.hasValue(value)) {
                user.sendMessage(CC.RED + "Value of option " + CC.BOLD + option.getName() + CC.RED + " is already set to " + CC.BOLD + value + CC.RED + '.');
                return;
            }
            option.setValue(value);
            OptionEditor.remove(user);
            return;
        }
        StringBuilder builder = new StringBuilder();
        boolean inHostChat = user.isGameModerator() && user.isHostChat();
        boolean inSpectatorChat = user.inState(UHCUserState.SPECTATOR) || user.isSpectatorChat();

        if (inHostChat) {
            if (!user.getStaffData().isHostChatMessages()) {
                user.sendMessage(CC.RED + "You're trying to speak in the " + CC.BOLD + "host" + CC.RED + " chat, but have disabled these messages in your settings.");
                return;
            }
            user.sendHostMessage(event.getMessage());
            return;
        }

        if (inSpectatorChat) {
            if (gameService.isSpectatorChatMuted() && !user.isGameModerator()) {
                user.sendMessage(CC.RED + "Spectator chat is muted at the moment.");
                return;
            }
            if (!user.getStaffData().isSpectatorChatMessages()) {
                user.sendMessage(CC.RED + "You're trying to speak in the " + CC.BOLD + "spectator" + CC.RED + " chat, but have disabled these messages in your settings.");
                return;
            }
            builder.append(CC.GRAY).append("[Spectator] ");
        }
        Team team = user.getTeam();
        if (gameService.isTeamGame() && team != null && !user.isSpectating()) {
            builder.append(CC.GRAY)
                    .append('[')
                    .append(team.getColor())
                    .append("Team #")
                    .append(team.getId())
                    .append(CC.GRAY)
                    .append("] ");
        }
        builder.append(user.getState().getStaffLongPrefix())
                .append(user.isGameModerator() ? user.getFullRealName() : user.getFullDisplayName())
                .append(CC.GRAY)
                .append(": ");
        if (!inSpectatorChat) {
            builder.append(CC.WHITE);
        }
        builder.append(event.getMessage());

        userService.getOnlineUsers()
                .stream()
                .filter(onlineUser -> {
                    // We're doing if statements like this, so it's easier to understand
                    if (onlineUser.getCoreData().isIgnoring(user.getUniqueId()) && !user.isEqualOrAbove(RankType.STAFF)) {
                        return false;
                    }
                    if (onlineUser.inState(UHCUserState.IN_GAME) && inSpectatorChat && gameService.isStateOrLower(GameState.PLAYING)) {
                        return false;
                    }
                    if (onlineUser.isGameModerator() && !onlineUser.getStaffData().isSpectatorChatMessages() && inSpectatorChat) {
                        return false;
                    }
                    return true;
                })
                .forEach(onlineUser -> onlineUser.sendMessage(builder.toString()));
    }
}
