package net.exemine.core.provider.nametag;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;

import java.util.ArrayList;
import java.util.List;

@Getter
public class NametagInfo {

    private final String name;
    private final String prefix;
    private final String suffix;
    private final List<String> members = new ArrayList<>();
    private final PacketPlayOutScoreboardTeam teamAddPacket;

    protected NametagInfo(String name, String prefix, String suffix) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;

        teamAddPacket = new PacketPlayOutScoreboardTeam(name, prefix, suffix, new ArrayList<>(), 0);
    }

    public void addMember(String member) {
        members.add(member);
    }

    public boolean hasMember(String name) {
        return members.contains(name);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof NametagInfo) {
            NametagInfo otherNametag = (NametagInfo) other;
            return name.equals(otherNametag.name) && prefix.equals(otherNametag.prefix) && suffix.equals(otherNametag.suffix);
        }
        return false;
    }
}