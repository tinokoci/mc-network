package net.exemine.core.user;

import lombok.RequiredArgsConstructor;
import net.exemine.api.controller.ApiController;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.rank.menu.RankReasonMenu;
import net.exemine.core.rank.procedure.RankProcedure;
import net.exemine.core.rank.procedure.RankProcedureState;
import net.exemine.core.user.base.UserService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class UserListener implements Listener {

    private final UserService<CoreUser, CoreData> userService;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!ApiController.getInstance().isBooted()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "This instance is booting up, please wait a bit before joining again.");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRankProcedure(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        CoreUser user = userService.get(event.getPlayer());
        String message = event.getMessage();
        RankProcedure procedure = RankProcedure.getProcedure(user);

        if (procedure == null) return;
        event.setCancelled(true);

        if (message.equalsIgnoreCase("cancel")) {
            procedure.cancel();
            return;
        }
        switch (procedure.getState()) {
            case DURATION:
                procedure.setDuration(TimeUtil.getMillisFromInput(message));
                procedure.setState(RankProcedureState.REASON);
                user.sendMessage(CC.PURPLE + "[Rank] " + CC.GRAY + "You have set " + CC.GOLD + TimeUtil.getNormalDuration(procedure.getDuration()) + CC.GRAY + " as the duration.");

                CoreUser coreUser = user.getCoreUser();
                new RankReasonMenu(coreUser).open();
                break;
            case REASON:
                user.sendMessage(CC.PURPLE + "[Rank] " + CC.GRAY + "You have set " + CC.GOLD + message + CC.GRAY + " as the reason.");
                procedure.setReason(message);
                procedure.confirm();
        }
    }
}
