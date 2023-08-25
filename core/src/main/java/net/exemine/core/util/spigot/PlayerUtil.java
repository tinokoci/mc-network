package net.exemine.core.util.spigot;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.exemine.api.texture.TextureEntry;
import net.exemine.api.util.ReflectionUtil;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PlayerUtil {

    public static TextureEntry getTextureEntry(GameProfile gameProfile) {
        Property property = gameProfile.getProperties().get("textures").iterator().next();
        return new TextureEntry(gameProfile.getId().toString().replace("-", ""), gameProfile.getName(), property.getValue(), property.getSignature());
    }

    public static Property getTextureProperty(TextureEntry entry) {
        return new Property("textures", entry.getValue(), entry.getSignature());
    }

    public static void updateTextures(GameProfile gameProfile, TextureEntry entry) {
        PropertyMap properties = gameProfile.getProperties();
        properties.get("textures").clear();
        properties.put("textures", getTextureProperty(entry));
    }

    public static void setGameProfileName(GameProfile gameProfile, String name) {
        ReflectionUtil.setFieldValue(gameProfile, "name", name);
    }

    public static void reset(Player player) {
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setSaturation(12.8F);
        player.setMaximumNoDamageTicks(20);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);
        player.setLevel(0);
        player.setExp(0.0F);
        player.setWalkSpeed(0.2F);
        player.setFlySpeed(0.1F);
        player.getInventory().setHeldItemSlot(0);
        player.setAllowFlight(false);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.closeInventory();
        player.setGameMode(GameMode.SURVIVAL);
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
        ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0);
        player.getOpenInventory().getTopInventory().clear();
        player.updateInventory();
    }
}
