package net.exemine.core.util.item;

import net.exemine.api.util.spigot.ChatColor;
import net.exemine.api.util.string.CC;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    public static final String GOLDEN_HEAD_NAME = CC.GOLD + "Golden Head";

    private static final List<ChatColor> WOOL_COLORS = new ArrayList<>(List.of(
            ChatColor.WHITE, ChatColor.GOLD, ChatColor.LIGHT_PURPLE, ChatColor.AQUA,
            ChatColor.YELLOW, ChatColor.GREEN, ChatColor.LIGHT_PURPLE, ChatColor.DARK_GRAY,
            ChatColor.GRAY, ChatColor.DARK_AQUA, ChatColor.DARK_PURPLE, ChatColor.BLUE,
            ChatColor.BLACK, ChatColor.DARK_GREEN, ChatColor.RED, ChatColor.BLACK));

    public static int getWoolData(ChatColor color) {
        if (color == ChatColor.DARK_RED) color = ChatColor.RED;
        if (color == ChatColor.DARK_BLUE) color = ChatColor.BLUE;

        return WOOL_COLORS.indexOf(color);
    }

    public static int getWoolData(String input) {
        return getWoolData(ChatColor.getByChar(input.substring(1, 2).charAt(0)));
    }

    public static int getGreen() {
        return getWoolData(ChatColor.GREEN);
    }

    public static int getPurple() {
        return getWoolData(ChatColor.DARK_PURPLE);
    }

    public static int getYellow() {
        return getWoolData(ChatColor.YELLOW);
    }

    public static int getRed() {
        return getWoolData(ChatColor.RED);
    }

    public static boolean isArmor(Material material) {
        return material.name().endsWith("HELMET") ||
                material.name().endsWith("CHESTPLATE") ||
                material.name().endsWith("LEGGINGS") ||
                material.name().endsWith("BOOTS");
    }

    public static boolean isGoldenHead(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(GOLDEN_HEAD_NAME);
    }

    public static ItemStack[] deserializeItemArray(String data) {
        if (data == null) return null;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return items;
        } catch (ClassNotFoundException | IOException e) {
            return null;
        }
    }

    public static String serializeItemArray(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * We need this method because 1.8 Spigot behaviour is retarded and both
     * World#dropItem + World#dropItemNaturally cause dropped items to be flying around
     *
     * @param itemStack Item to drop
     * @param block Origin block
     * @param player Recipient
     */
    public static void dropItem(ItemStack itemStack, Block block, Player player) {
        Item item = block.getWorld().dropItem(block.getLocation().clone().add(0.5, 0.3, 0.5), itemStack);
        Vector vector = new Vector(
                (player.getLocation().getX() - block.getLocation().getX()) * 0.025,
                ((player.getLocation().getY() - block.getLocation().getY()) * 0.01) + 0.15,
                (player.getLocation().getZ() - block.getLocation().getZ()) * 0.025);
        item.setVelocity(vector);
    }
}
