package net.exemine.core.lunar.impl;

import com.lunarclient.bukkitapi.nethandler.LCPacket;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketWorldBorder;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketWorldBorderRemove;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.exemine.core.lunar.LunarModule;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;

@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class LunarBorder extends LunarModule {

    public static void remove(Player player) {
        new BorderRemoval().send(player);
    }

    private final World world;
    private final int radius;

    @Override
    protected Set<LCPacket> getPackets() {
        return Collections.singleton(
                new LCPacketWorldBorder("border",
                        world.getName(),
                        true,
                        false,
                        Color.GRAY.asRGB() - 10,
                        -radius + 1, -radius + 1, radius - 1, radius - 1));
    }

    static class BorderRemoval extends LunarModule {
        @Override
        protected Set<LCPacket> getPackets() {
            return Collections.singleton(new LCPacketWorldBorderRemove("border"));
        }
    }
}
