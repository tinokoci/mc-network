package net.exemine.core.nms;

import com.execets.spigot.handler.PacketHandler;
import net.exemine.core.nms.npc.NPC;
import net.exemine.core.nms.npc.NPCInteractEvent;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;

public class NMSPacketHandler implements PacketHandler {

    @Override
    public void handleReceivedPacket(PlayerConnection playerConnection, Packet packet) {
        if (packet instanceof PacketPlayInUseEntity) {
            PacketPlayInUseEntity playInUseEntityPacket = (PacketPlayInUseEntity) packet;

            if (playInUseEntityPacket.a() != PacketPlayInUseEntity.EnumEntityUseAction.INTERACT) return;

            NPC npc = NPC.get(playInUseEntityPacket.getA());

            if (npc != null) {
                Bukkit.getPluginManager().callEvent(new NPCInteractEvent(playerConnection.getPlayer(), npc));
            }
        }
    }
}