package net.exemine.core.lunar.impl;

import com.lunarclient.bukkitapi.nethandler.LCPacket;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTitle;
import com.lunarclient.bukkitapi.title.TitleType;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.exemine.core.lunar.LunarModule;

import java.util.HashSet;
import java.util.Set;

@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class LunarTitle extends LunarModule {

    private final String message;

    private long displayTimeMs = 5000L, fadeInTimeMs = 750L, fadeOutTimeMs = 750L;
    private String subtitleMessage;

    @Override
    protected Set<LCPacket> getPackets() {
        Set<LCPacket> packets = new HashSet<>();

        packets.add(new LCPacketTitle(TitleType.TITLE.name(), message, displayTimeMs, fadeInTimeMs, fadeOutTimeMs));
        if (subtitleMessage != null) {
            packets.add(new LCPacketTitle(TitleType.SUBTITLE.name(), subtitleMessage, displayTimeMs, fadeInTimeMs, fadeOutTimeMs));
        }
        return packets;
    }
}
