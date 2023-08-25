package net.exemine.uhc.border.glass.interceptor;

import com.execets.spigot.handler.MovementHandler;
import com.lunarclient.bukkitapi.LunarClientAPI;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.impl.CoreData;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.border.glass.GlassBorderService;
import net.exemine.uhc.location.LocationService;
import net.exemine.uhc.world.WorldService;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class GlassBorderMoveInterceptor implements MovementHandler {

    private final BorderService borderService;
    private final GlassBorderService glassBorderService;
    private final LocationService locationService;
    private final WorldService worldService;
    private final UserService<CoreUser, CoreData> userService;

    @Override
    public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packet) {
        if (LunarClientAPI.getInstance().isRunningLunarClient(player) &&
                userService.get(player).getData().getLunarData().isBorder()) {
            return;
        }
        World world = player.getWorld();
        int radius = world == worldService.getUhcWorld() ? borderService.getCurrentRadius().getValue()
                : world == worldService.getNetherWorld() ? borderService.getNetherBorder()
                : world == worldService.getPracticeWorld() ? locationService.getPracticeMapRadius()
                : -1;
        if (radius == -1) return;
        glassBorderService.updateLocations(player, radius);
    }
}
