package net.exemine.core.nms.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import net.exemine.api.texture.TextureEntry;
import net.exemine.api.util.Executor;
import net.exemine.api.util.MathUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import net.exemine.core.nms.hologram.Hologram;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.util.spigot.PlayerUtil;
import net.exemine.core.util.team.TeamPriority;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Getter
public class NPC {

    private static final Map<Integer, NPC> NPCS = new HashMap<>();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static int dynamicNameIndex = 0;

    private final Set<UUID> currentlySpawningFor = ConcurrentHashMap.newKeySet();
    private final List<UUID> packetTracker = new ArrayList<>();

    private final int entityId;
    private final String skin;

    @Setter
    private Location location;

    private String displayName;
    private EntityPlayer entity;
    private PacketPlayOutScoreboardTeam packet;

    private String dynamicIdentifier;
    private boolean useDynamicName;

    private Hologram attachedHologram;

    private final ItemStack[] equipment = new ItemStack[5];

    public NPC(String displayName, String skin, Location location, boolean useDynamicName) {
        this.entityId = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        this.skin = skin;
        this.location = location;

        if (useDynamicName) {
            String dynamicName = getGameProfileName();

            if (dynamicName != null) {
                this.useDynamicName = true;
                this.dynamicIdentifier = dynamicName;

                packet = new PacketPlayOutScoreboardTeam();
                packet.a = TeamPriority.NPC + "-npc-" + entityId;
                packet.e.add(dynamicIdentifier);
            }
        }
        this.displayName = displayName.length() > 16 && !this.useDynamicName ? displayName.substring(0, 16) : displayName;

        NPCS.put(entityId, this);
    }

    public void spawn(ExeUser<?> user) {
        if (!user.inWorld(location.getWorld()) || currentlySpawningFor.contains(user.getUniqueId())) return;
        currentlySpawningFor.add(user.getUniqueId());

        boolean usePacketTracker = useDynamicName && !packetTracker.contains(user.getUniqueId());
        if (usePacketTracker) {
            packetTracker.add(user.getUniqueId());
        }
        EXECUTOR.execute(() -> {
            GameProfile profile = new GameProfile(UUID.randomUUID(), useDynamicName ? dynamicIdentifier : displayName);
            Property property = user.getCraftPlayer().getProfile().getProperties().get("textures").iterator().next();

            if (skin != null) {
                TextureEntry entry = user.getCore().getTextureService().getOrFetch(skin);
                if (entry != null) property = PlayerUtil.getTextureProperty(entry);
            }
            profile.getProperties().clear();
            profile.getProperties().put("textures", property);

            WorldServer world = ((CraftWorld) (user.getWorld())).getHandle();

            entity = new EntityPlayer(world.getMinecraftServer(), world, profile, new PlayerInteractManager(world));
            entity.collidesWithEntities = false;
            entity.d(entityId);

            byte compressedYaw = MathUtil.getCompressedAngle(location.getYaw());
            byte compressedPitch = MathUtil.getCompressedAngle(location.getPitch());

            PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(entity);

            spawnPacket.c = MathHelper.floor(location.getX() * 32.0D);
            spawnPacket.d = MathHelper.floor(location.getY() * 32.0D);
            spawnPacket.e = MathHelper.floor(location.getZ() * 32.0D);

            PacketPlayOutEntity.PacketPlayOutEntityLook bodyPositionPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(entityId, compressedYaw, compressedPitch, true);
            PacketPlayOutEntityHeadRotation headPositionPacket = new PacketPlayOutEntityHeadRotation(entity, compressedYaw);

            user.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity));   // Adding user to the server
            user.sendPacket(spawnPacket);                                 // Spawning user to the location
            user.sendPacket(bodyPositionPacket);                          // Updating user's body position
            user.sendPacket(headPositionPacket);                          // Updating user's head position

            if (usePacketTracker) {
                updatePacketPrefixAndSuffix(displayName, user, true); // Scoreboard bs for glitchless name updates
            }
            if (user.is1_8()) {
                DataWatcher watcher = entity.getDataWatcher();
                watcher.watch(10, (byte) 127);

                PacketPlayOutEntityMetadata secondSkinLayerPacket = new PacketPlayOutEntityMetadata(entityId, watcher, true);
                user.sendPacket(secondSkinLayerPacket);                   // Enables user's second skin layer (127 -> sum of all bit masks for skin parts)
            }
            for (int i = 0; i < equipment.length; i++) {
                ItemStack item = equipment[i];

                if (item != null)
                    user.sendPacket(updateEquipment(i, item));            // Updates user's equipment
            }
            // Remove user from tablist
            // Waiting 2 seconds on 1.8 because on that version if you remove the player before the skin was
            // fetched (or whatever happens don't really know) it displays the default skin
            Executor.schedule(() -> {
                user.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entity));
                currentlySpawningFor.remove(user.getUniqueId());
            }).runAsyncLater(user.is1_7() ? 0L : 3000L);
        });
    }

    public void spawn() {
        Core.get().getUserService().getOnlineUsers().forEach(this::spawn);
    }

    public void cleanup(ExeUser<?> user) {
        packetTracker.remove(user.getUniqueId());
    }

    public void rename(String name) {
        if (!useDynamicName || packet == null || displayName.equals(name)) return;

        this.displayName = name;

        EXECUTOR.execute(() -> Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> packetTracker.contains(player.getUniqueId()))
                .forEach(player -> updatePacketPrefixAndSuffix(name, player, false))
        );
    }

    public NPC attachHologram(Hologram hologram) {
        attachedHologram = hologram;

        hologram.destroy(false);
        hologram.setLocation(getLocation().clone().add(0, 2.09 + Hologram.SEPARATOR_HEIGHT + (hologram.getBelow().size() * Hologram.SEPARATOR_HEIGHT), 0));

        IntStream.range(0, hologram.getBelow().size()).forEach(i -> {
            Hologram below = hologram.getBelow().get(i);
            below.setLocation(hologram.getLocation().clone().add(0, -Hologram.SEPARATOR_HEIGHT - (i * Hologram.SEPARATOR_HEIGHT), 0));
        });
        IntStream.range(0, hologram.getAbove().size()).forEach(i -> {
            Hologram above = hologram.getAbove().get(i);
            above.setLocation(hologram.getLocation().clone().add(0, Hologram.SEPARATOR_HEIGHT + (i * Hologram.SEPARATOR_HEIGHT), 0));
        });
        return this;
    }

    public NPC setItemInHand(ItemStack item) {
        equipment[0] = item;
        return this;
    }

    public NPC setHelmet(ItemStack item) {
        equipment[1] = item;
        return this;
    }

    public NPC setChestplate(ItemStack item) {
        equipment[2] = item;
        return this;
    }

    public NPC setLeggings(ItemStack item) {
        equipment[3] = item;
        return this;
    }

    public NPC setBoots(ItemStack item) {
        equipment[4] = item;
        return this;
    }

    private void updatePacketPrefixAndSuffix(String name, Player player, boolean create) {
        packet.f = create ? 0 : 2;

        if (name.length() > 16) {
            int split = name.charAt(15) == ChatColor.COLOR_CHAR ? 15 : 16;

            String prefix = name.substring(0, split);
            String lastColor = ChatColor.getLastColors(prefix);
            String suffix = (StringUtils.isEmpty(lastColor) ? CC.RESET : lastColor) + name.substring(split);

            packet.c = prefix;
            packet.d = suffix.length() > 16 ? suffix.substring(0, 16) : suffix;
        } else {
            packet.c = name;
            packet.d = "";
        }
        player.sendPacket(packet);
    }

    private PacketPlayOutEntityEquipment updateEquipment(int index, ItemStack item) {
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment();

        packet.a = entityId;
        packet.b = index;
        packet.c = CraftItemStack.asNMSCopy(item);

        return packet;
    }

    @Nullable
    private String getGameProfileName() {
        // If my math is correct, we could potentially make an algorithm to combine 8 random colors
        // since 16 chars for name is max which would allow us to have 12 million NPCs with "dynamic" names
        if (++dynamicNameIndex >= ChatColor.values().length) {
            return null;
        } else {
            return CC.translate("&" + ChatColor.values()[dynamicNameIndex].getChar());
        }
    }

    @Nullable
    public static NPC get(int entityID) {
        return NPCS.get(entityID);
    }

    public static Collection<NPC> getNPCS() {
        return NPCS.values();
    }
}