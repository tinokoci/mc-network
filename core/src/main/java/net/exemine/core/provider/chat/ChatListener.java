package net.exemine.core.provider.chat;

import lombok.RequiredArgsConstructor;
import net.exemine.api.data.ExeData;
import net.exemine.api.model.Channel;
import net.exemine.api.punishment.Punishment;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.rank.RankType;
import net.exemine.api.redis.RedisService;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.server.ServerService;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.InstanceUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ChatListener<U extends ExeUser<D>, D extends ExeData> implements Listener {

    private final ChatService<U, D> chatService;
    private final UserService<U, D> userService;
    private final RedisService redisService;
    private final ServerService serverService;

    private final Map<UUID, Long> chatCooldown = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        event.setCancelled(true);

        U user = userService.get(event.getPlayer());
        if (sendCustomChatMessage(user.getCoreUser(), event.getMessage())) return;

        if (serverService.isChatMuted() && !user.isEqualOrAbove(RankType.STAFF)) {
            user.sendMessage(CC.RED + "You cannot speak while public chat is muted.");
            return;
        }
        Punishment punishment = user.getBulkData().getActivePunishment(PunishmentType.MUTE);

        if (punishment != null) {
            user.sendMessage(CC.RED + "You are currently suspended from the chat.");
            user.sendMessage(CC.RED + (punishment.isPermanent() ? "This punishment will not expire." : "Your punishment expires in " + punishment.getExpirationTime() + '.'));
            return;
        }
        if (isCooldownActive(user)) return;

        chatService.getAdapter().sendMessage(user, user.getData(), event);
    }

    private boolean sendCustomChatMessage(CoreUser user, String chatMessage) {
        Channel channel = user.getData().getChannel();
        if (channel == Channel.DEFAULT) return false;

        if (user.isEqualOrAbove(channel.getRank())) {
            if (!user.getStaffData().isChatMessages()) {
                user.sendMessage(CC.RED + "You have staff messages turned off.");
                return true;
            }
            String message = channel.getChatFormat(InstanceUtil.getCurrent(), user.getColoredRealName(), chatMessage);
            redisService.getPublisher().sendAlertStaffMessage(channel.getRank(), message);
            return true;
        }
        user.toggleChannel(channel);
        return false;
    }

    private boolean isCooldownActive(U user) {
        long chatDelay = serverService.getChatDelay();
        if (user.isEqualOrAbove(RankType.DONATOR) || chatDelay == 0L) return false;

        long cooldown = chatCooldown.getOrDefault(user.getUniqueId(), -1L);

        if (cooldown != -1 && cooldown > System.currentTimeMillis()) {
            user.sendMessage(CC.RED + "Please wait " + TimeUtil.getShortDuration(cooldown - System.currentTimeMillis()) + " to chat again.");
            return true;
        }
        chatCooldown.put(user.getUniqueId(), System.currentTimeMillis() + chatDelay);
        return false;
    }
}
