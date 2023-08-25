package net.exemine.core.provider.chat;

import lombok.Getter;
import net.exemine.api.data.ExeData;
import net.exemine.api.redis.RedisService;
import net.exemine.core.provider.ChatProvider;
import net.exemine.core.server.ServerService;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ChatService<U extends ExeUser<D>, D extends ExeData> {

    private final ChatProvider<U, D> adapter;
    private final UserService<U, D> userService;

    public ChatService(JavaPlugin plugin,
                       UserService<U, D> userService,
                       ChatProvider<U, D> adapter,
                       RedisService redisService,
                       ServerService serverService) {
        this.adapter = adapter;
        this.userService = userService;

        ChatListener<U, D> listener = new ChatListener<>(this, userService, redisService, serverService);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
}
