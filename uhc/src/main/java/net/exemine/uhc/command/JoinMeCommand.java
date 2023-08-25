package net.exemine.uhc.command;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.redis.RedisService;
import net.exemine.api.util.Cooldown;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.util.InstanceUtil;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.scatter.late.LateScatterTime;
import net.exemine.uhc.user.UHCUser;

import java.util.List;
import java.util.UUID;

public class JoinMeCommand extends BaseCommand<UHCUser, UHCData> {

    private final RedisService redisService;
    private final GameService gameService;
    private final Cooldown<UUID> cooldown = new Cooldown<>();

    public JoinMeCommand(RedisService redisService, GameService gameService) {
        super(List.of("joinme", "announce"), Rank.PRIME);
        this.redisService = redisService;
        this.gameService = gameService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (gameService.isNotState(GameState.LOBBY) && System.currentTimeMillis() - gameService.getStartTime() > LateScatterTime.DEFAULT.getTime()) {
            user.sendMessage(CC.RED + "You cannot do that in the current game state.");
            return;
        }
        if (ToggleOption.WHITELIST.isEnabled()) {
            user.sendMessage(CC.RED + "Whitelist is currently enabled.");
            return;
        }
        if (cooldown.isActive(user.getUniqueId())) {
            user.sendMessage(CC.RED + "Please wait " + CC.BOLD + cooldown.getNormalDuration(user.getUniqueId()) + CC.RED + " before doing that again.");
            return;
        }
        cooldown.put(user.getUniqueId(), 60);
        redisService.getPublisher().sendAlertUHCAnnounce(CC.GRAY + '[' + CC.BOLD_PURPLE + '✠' + CC.GRAY + "] " + user.getColoredDisplayName() + CC.GOLD + " wants you to come play " + CC.BOLD_PINK + "UHC" + CC.GOLD + "! ",
                CC.GREEN + "(Click to join)",
                CC.GREEN + "Click to play uhc!",
                "/join " + InstanceUtil.getName()
        );
    }
}
