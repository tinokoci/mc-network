package net.exemine.uhc.hook;

import net.exemine.api.util.string.CC;
import net.exemine.core.user.base.ExeUser;
import net.exemine.uhc.UHC;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HookService {

    private final UHC plugin;
    private final GameService gameService;
    private final UHCUserService userService;

    public HookService(UHC plugin) {
        this.plugin = plugin;
        this.gameService = plugin.getGameService();
        this.userService = plugin.getUserService();
        setupListCallback();
    }

    private void setupListCallback() {
        plugin.getCore().getServerService().setListCallback(() -> {
            List<String> list = new ArrayList<>();

            // Build Moderators String
            List<UHCUser> mods = userService.getModUsers();

            boolean hasHost = gameService.getHost() != null;
            boolean hasSupervisor = gameService.isSupervised();

            int count = mods.size();
            if (hasHost) ++count;
            if (hasSupervisor) ++count;

            if (count > 0) {
                StringBuilder builder = new StringBuilder();

                builder.append(CC.GRAY).append("Moderators (").append(count).append("): ");

                if (hasHost) {
                    builder.append(gameService.getFormattedHost())
                            .append(CC.DARK_GRAY)
                            .append(" [H]");
                }
                if (hasSupervisor) {
                    if (hasHost) builder.append(CC.GRAY).append(", ");
                    builder.append(gameService.getFormattedSupervisor())
                            .append(CC.DARK_GRAY)
                            .append(" [S]");
                }

                if (!mods.isEmpty()) {
                    if (hasHost || hasSupervisor) builder.append(CC.GRAY).append(", ");
                    builder.append(mods
                            .stream()
                            .map(ExeUser::getColoredRealName)
                            .collect(Collectors.joining(CC.GRAY + ", "))
                    );
                }
                list.add(builder.toString());
            }
            Comparator<UHCUser> byDisguise = Comparator.comparing(ExeUser::isDisguised);
            Comparator<UHCUser> byPriority = Comparator.comparing(user -> user.getRank().getPriority());

            // Add Participants
            List<UHCUser> participants = userService.getOnlineUsers()
                    .stream()
                    .filter(user -> !user.isSpectating())
                    .collect(Collectors.toList());

            if (!participants.isEmpty()) {
                list.add(CC.GRAY + "Participants (" + participants.size() + "): "
                        + participants
                        .stream()
                        .sorted(byDisguise.thenComparing(byPriority))
                        .map(UHCUser::getColoredDisplayName)
                        .collect(Collectors.joining(CC.GRAY + ", ")));
            }

            // Add Spectators
            List<UHCUser> spectators = userService.getOnlineUsers()
                    .stream()
                    .filter(UHCUser::isRegularSpectator)
                    .collect(Collectors.toList());

            if (!spectators.isEmpty()) {
                list.add(CC.GRAY + "Spectators (" + spectators.size() + "): "
                        + spectators
                        .stream()
                        .sorted(byDisguise.thenComparing(byPriority))
                        .map(UHCUser::getColoredDisplayName)
                        .collect(Collectors.joining(CC.GRAY + ", ")));
            }
            return list;
        });
    }
}
