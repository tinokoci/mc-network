package net.exemine.core.lunar.impl;

import com.lunarclient.bukkitapi.nethandler.LCPacket;
import com.lunarclient.bukkitapi.nethandler.shared.LCPacketWaypointAdd;
import com.lunarclient.bukkitapi.nethandler.shared.LCPacketWaypointRemove;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.exemine.core.lunar.LunarModule;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;

@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class LunarWaypoint extends LunarModule {

    public static void remove(Player player, String waypointName) {
        new WaypointRemoval(waypointName, player.getWorld()).send(player);
    }

    private final String name;
    private final Location location;

    @Override
    protected Set<LCPacket> getPackets() {
        return Collections.singleton(
                new LCPacketWaypointAdd(name,
                        location.getWorld().getName(),
                        Color.GRAY.asRGB(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        false,
                        true));
    }

    @RequiredArgsConstructor
    static class WaypointRemoval extends LunarModule {

        private final String name;
        private final World world;

        @Override
        protected Set<LCPacket> getPackets() {
            return Collections.singleton(new LCPacketWaypointRemove(name, world.getName()));
        }
    }
}
