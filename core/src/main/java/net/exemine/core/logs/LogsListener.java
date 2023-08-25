package net.exemine.core.logs;

import lombok.RequiredArgsConstructor;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.log.LogService;
import net.exemine.api.log.minecraft.MinecraftLogType;
import net.exemine.api.util.Executor;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.logs.procedure.LogsProcedure;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.InstanceUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class LogsListener implements Listener {

    private final UserService<CoreUser, CoreData> userService;
    private final LogService logService;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        CoreUser user = userService.get(event.getPlayer());
        MinecraftLogType type = Stream.of("message", "msg", "m", "tell", "w", "whisper", "reply", "r").anyMatch(command -> event.getMessage().startsWith('/' + command + ' '))
                ? MinecraftLogType.PRIVATE
                : MinecraftLogType.COMMAND;
        Executor.schedule(() -> logService.insertMinecraftLog(type, user.getUniqueId(), user.isDisguised(), InstanceUtil.getCurrent(), user.getDisplayName(), " executed " + event.getMessage())).runAsync();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        CoreUser user = userService.get(event.getPlayer());
        Executor.schedule(() -> logService.insertMinecraftLog(MinecraftLogType.PUBLIC, user.getUniqueId(), user.isDisguised(), InstanceUtil.getCurrent(), user.getDisplayName(), ": " + event.getMessage())).runAsync();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRankProcedure(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        CoreUser user = userService.get(event.getPlayer());
        String message = event.getMessage();
        LogsProcedure procedure = LogsProcedure.getProcedure(user);

        if (procedure == null) return;
        event.setCancelled(true);

        if (message.equalsIgnoreCase("cancel")) {
            procedure.cancel();
            return;
        }
        if (!TimeUtil.isDate(message)) {
            user.sendMessage(CC.RED + "That's not a valid time format.");
            return;
        }
        long timestamp = TimeUtil.getMillisFromDate(message);

        switch (procedure.getState()) {
            case START_TIMESTAMP:
                procedure.setStartTimestamp(timestamp);
                user.sendMessage(CC.PURPLE + "[Logs] " + CC.GRAY + "You have set " + CC.GOLD + message + CC.GRAY + " as the start time.");
                break;
            case END_TIMESTAMP:
                procedure.setEndTimestamp(timestamp);
                user.sendMessage(CC.PURPLE + "[Logs] " + CC.GRAY + "You have set " + CC.GOLD + message + CC.GRAY + " as the end time.");
        }
        new LogsMenu(user, procedure.getTarget()).open();
    }
}
