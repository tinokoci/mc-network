package net.exemine.uhc.practice;

import com.execets.spigot.handler.MovementHandler;
import lombok.RequiredArgsConstructor;
import net.exemine.uhc.location.LocationService;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;
import net.exemine.uhc.world.WorldService;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PracticeMoveInterceptor implements MovementHandler {

    private final LocationService locationService;
    private final UHCUserService userService;

    @Override
    public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packet) {
        UHCUser user = userService.get(player);
        if (!user.inWorld(WorldService.PRACTICE_WORLD_NAME)) return;

        if (from.getY() < 15) {
            user.teleport(locationService.getPracticeScatterLocation());
            user.setState(UHCUserState.PRACTICE);
        }
    }
}
