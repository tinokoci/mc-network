package net.exemine.uhc.lobby;

import com.execets.spigot.handler.MovementHandler;
import lombok.RequiredArgsConstructor;
import net.exemine.api.util.Cooldown;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.particle.ParticleEffect;
import net.exemine.uhc.location.LocationService;
import net.exemine.uhc.world.WorldService;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

@RequiredArgsConstructor
public class LobbyMovementHandler implements MovementHandler {

    private final LocationService locationService;
    private final WorldService worldService;

    private final Cooldown<UUID> pushCooldown = new Cooldown<>();
    private final Cooldown<UUID> messageCooldown = new Cooldown<>();

    @Override
    public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packet) {
        if (player.getWorld() != worldService.getLobbyWorld()) return;

        if (from.getY() < locationService.getLobbyWhooshY()) {
            int x = from.getBlockX();
            int z = from.getBlockZ();
            Location highestLocation = locationService.getHighestLocationInWorld(worldService.getLobbyWorld(), x, z);

            if (highestLocation.getY() > 0) {
                Location playerLocation = player.getLocation();

                highestLocation.setYaw(playerLocation.getYaw());
                highestLocation.setPitch(playerLocation.getPitch());
                highestLocation.setY(highestLocation.getY() + 10);
                player.teleport(highestLocation);

                sendMessage(player);
                ParticleEffect.EXPLOSION_HUGE.display(0, 0, 0, 0, 1, highestLocation, 256);
                return;
            }
            if (pushCooldown.isActive(player.getUniqueId())) return;
            pushCooldown.put(player.getUniqueId(), 3);

            ParticleEffect.EXPLOSION_HUGE.display(0, 0, 0, 0, 1, player.getLocation(), 256);
            player.setVelocity(new Vector(0, 8, 0));
            sendMessage(player);
        }
    }

    private void sendMessage(Player player) {
        if (messageCooldown.isActive(player.getUniqueId())) return;
        messageCooldown.put(player.getUniqueId(), 15);
        player.sendMessage(CC.ITALIC_GRAY + "Whoosh!");
    }
}
