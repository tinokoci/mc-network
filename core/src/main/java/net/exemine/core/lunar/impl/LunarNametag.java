package net.exemine.core.lunar.impl;

import com.lunarclient.bukkitapi.nethandler.LCPacket;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketNametagsOverride;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.exemine.core.lunar.LunarModule;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class LunarNametag extends LunarModule {

    public static void remove(Player player, Player target) {
        new LunarNametag(target.getUniqueId(), new ArrayList<>()).send(player);
    }

    private final UUID player;
    private final List<String> nametag;

    @Override
    protected Set<LCPacket> getPackets() {
        return Collections.singleton(new LCPacketNametagsOverride(player, nametag));
    }
}
