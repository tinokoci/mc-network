package net.exemine.core.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.cache.RedisCache;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class LinkCommand extends BaseCommand<CoreUser, CoreData> {

    private final RedisService redisService;

    public LinkCommand(RedisService redisService) {
        super(List.of("link", "sync"));
        this.redisService = redisService;
        setAsync(true);
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        if (data.isDiscordLinked()) {
            user.sendMessage(CC.RED + "You are already discord linked.");
            return;
        }
        if (args.length == 0 || !args[0].equalsIgnoreCase("accept")) {
            if (!user.getDiscordLinkRequest(redisService)) {
                user.sendMessage(CC.RED + "You don't have a pending discord link request.");
            }
            return;
        }
        redisService.deleteValueFromHash(RedisCache.DISCORD_LINK, user.getUniqueId());

        data.setDiscordUserId(user.getDiscordLinkModel().getUserId());
        user.saveData(false);
        user.sendDiscordUpdate();
        user.sendMessage(CC.PURPLE + "[Link] " + CC.GRAY + "You are now linked to a discord account.");
    }
}
