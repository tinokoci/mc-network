package net.exemine.core.lunar.impl;

import com.lunarclient.bukkitapi.nethandler.LCPacket;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTeammates;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.exemine.core.lunar.LunarModule;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class LunarTeamView extends LunarModule {

    public static void remove(Player player) {
        new LunarTeamView(null, new ArrayList<>()).send(player);
    }

    private final UUID leader;
    private final List<Player> teammates;

    private long lastMs = 0L;

    @Override
    protected Set<LCPacket> getPackets() {
        Map<UUID, Map<String, Double>> playerMap = new HashMap<>();

        teammates.forEach(mate -> {
            Map<String, Double> map = new HashMap<>();
            map.put("x", mate.getLocation().getX());
            map.put("y", mate.getLocation().getY());
            map.put("z", mate.getLocation().getZ());
            playerMap.put(mate.getUniqueId(), map);
        });
        return Collections.singleton(new LCPacketTeammates(leader, lastMs, playerMap));
    }
}
