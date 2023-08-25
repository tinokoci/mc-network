package net.exemine.uhc.vape;

import com.execets.spigot.handler.PacketHandler;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class AntiVapeSpecPacketHandler implements PacketHandler {

    @Override
    public void handleReceivedPacket(PlayerConnection connection, Packet unspecifiedPacket) {
        if (unspecifiedPacket instanceof PacketPlayOutEntityEquipment) {
            PacketPlayOutEntityEquipment packet = (PacketPlayOutEntityEquipment) unspecifiedPacket;
            ItemStack originalItem = CraftItemStack.asBukkitCopy(packet.c);
            ItemBuilder replaceWith = new ItemBuilder(originalItem.getType());

            if (!originalItem.getEnchantments().isEmpty()) {
                replaceWith.addEnchantment(Enchantment.DURABILITY, 4);
            }
            if (ItemUtil.isArmor(originalItem.getType())) {
                replaceWith.setDurability(1);
            }
            replaceWith.setAmount(64);
            packet.c = CraftItemStack.asNMSCopy(replaceWith.build());
        }
    }
}
