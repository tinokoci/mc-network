package net.exemine.uhc.border.glass.interceptor;

import com.execets.spigot.handler.PacketHandler;
import com.lunarclient.bukkitapi.LunarClientAPI;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.Executor;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import net.exemine.uhc.border.glass.GlassBorderService;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class GlassBorderPacketInterceptor implements PacketHandler {

    private final GlassBorderService glassBorderService;
    private final UserService<CoreUser, CoreData> userService;

    @Override
    public void handleReceivedPacket(PlayerConnection connection, Packet packet) {
        Player player = connection.getPlayer();
        if (LunarClientAPI.getInstance().isRunningLunarClient(player) &&
                userService.get(player).getData().getLunarData().isBorder()) {
            return;
        }
        if (packet instanceof PacketPlayInBlockDig) {
            PacketPlayInBlockDig digPacket = (PacketPlayInBlockDig) packet;
            if (digPacket.c() != PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK
                    && digPacket.c() != PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) return;

            Location location = new Location(player.getWorld(), digPacket.a().getX(), digPacket.a().getY(), digPacket.a().getZ());
            if (!glassBorderService.hasGlassAt(player, location)) return;

            Executor.Task task = Executor.schedule(() -> glassBorderService.sendGlassBlockUpdate(player, location));
            if (player.getGameMode() == GameMode.CREATIVE) {
                task.runAsyncLater(100L);
            } else {
                task.runSync();
            }
        } else if (packet instanceof PacketPlayInBlockPlace) {
            PacketPlayInBlockPlace placePacket = (PacketPlayInBlockPlace) packet;
            Location location = new Location(player.getWorld(), placePacket.a().getX(), placePacket.a().getY(), placePacket.a().getZ());
            if (!glassBorderService.hasGlassAt(player, location)) return;

            Executor.schedule(() -> glassBorderService.sendGlassBlockUpdate(player, location)).runAsyncLater(100L);
        }
    }
}
