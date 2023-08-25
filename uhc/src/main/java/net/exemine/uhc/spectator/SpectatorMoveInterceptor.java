package net.exemine.uhc.spectator;

import com.execets.spigot.handler.MovementHandler;
import lombok.RequiredArgsConstructor;
import net.exemine.api.util.Cooldown;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.LocationUtil;
import net.exemine.core.util.particle.ParticleEffect;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class SpectatorMoveInterceptor implements MovementHandler {

    private final UHCUserService userService;
    private final Cooldown<UUID> cooldown = new Cooldown<>();

    @Override
    public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packet) {
        UHCUser user = userService.get(player);
        if (user.isRegularSpectator() && !user.isSpectatorTeleportDelay() && LocationUtil.isOutsideRadius(to, 100)) {
            user.teleport(from);
            if (!cooldown.isActive(player.getUniqueId())) {
                cooldown.put(player.getUniqueId(), 3);
                ParticleEffect.SMOKE_LARGE.display(0, 0, 0, 0, 1, to, 256);
                user.playSound(Sound.EXPLODE);
                user.sendMessage(CC.RED + "You cannot spectate further than 100 radius.");
            }
        }
    }
}
