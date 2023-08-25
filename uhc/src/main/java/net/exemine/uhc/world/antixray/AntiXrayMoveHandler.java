package net.exemine.uhc.world.antixray;

import com.execets.spigot.handler.MovementHandler;
import lombok.RequiredArgsConstructor;
import net.exemine.uhc.user.UHCUserService;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class AntiXrayMoveHandler implements MovementHandler {

    private final AntiXrayService antiXrayService;
    private final UHCUserService uhcUserService;

    @Override
    public void handleUpdateLocation(Player player, Location from, Location to, PacketPlayInFlying packet) {
        antiXrayService.getThread().queueUpdate(uhcUserService.get(player), to);
    }
}
