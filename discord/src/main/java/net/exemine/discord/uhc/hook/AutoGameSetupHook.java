package net.exemine.discord.uhc.hook;

import lombok.RequiredArgsConstructor;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.redis.RedisService;

@RequiredArgsConstructor
public class AutoGameSetupHook {

    private final RedisService redisService;

    public void run(UHCMatch nextMatch) {
        if (nextMatch == null) return;
        redisService.getPublisher().sendUHCSetup(nextMatch);
    }
}

