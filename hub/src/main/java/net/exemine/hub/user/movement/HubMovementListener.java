package net.exemine.hub.user.movement;

import com.execets.spigot.handler.MovementHandler;
import lombok.RequiredArgsConstructor;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.core.util.LocationUtil;
import net.exemine.hub.user.HubUser;
import net.exemine.hub.user.HubUserService;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class HubMovementListener implements MovementHandler {

    private final HubUserService userService;
    private final int voidTeleportY;
    private final int mapRadius;

    public HubMovementListener(ConfigFile configFile, HubUserService userService) {
        this.userService = userService;
        this.voidTeleportY = configFile.getInt("void_teleport_y");
        this.mapRadius = configFile.getInt("map_radius");
    }

    @Override
    public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packet) {
        HubUser user = userService.get(player);

        if (player.getGameMode() != GameMode.CREATIVE
                && player.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR
                && !player.isFlying()
                && !user.getData().isFlight()) {
            player.setAllowFlight(true);
        }
        if (to.getY() < voidTeleportY || LocationUtil.isOutsideRadius(user, mapRadius)) {
            user.teleportToSpawn();
        }
    }
}