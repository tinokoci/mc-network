package net.exemine.core.disguise;

import com.mojang.authlib.GameProfile;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.rank.Rank;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.cache.RedisCache;
import net.exemine.api.redis.cache.model.DisguiseModel;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.texture.TextureEntry;
import net.exemine.api.texture.TextureService;
import net.exemine.api.util.CollectionUtil;
import net.exemine.api.util.Executor;
import net.exemine.api.util.MathUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.DatabaseUtil;
import net.exemine.core.Core;
import net.exemine.core.disguise.action.DisguiseAction;
import net.exemine.core.disguise.entry.DisguiseEntry;
import net.exemine.core.disguise.entry.DisguiseEntryType;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.util.ServerUtil;
import net.exemine.core.util.spigot.PlayerUtil;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bson.Document;
import org.bukkit.Location;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class DisguiseService {

    private final DatabaseService databaseService;
    private final RedisService redisService;
    private final TextureService textureService;

    private final Set<DisguiseEntry> disguiseNames = ConcurrentHashMap.newKeySet();
    private final Set<DisguiseEntry> disguiseSkins = ConcurrentHashMap.newKeySet();

    private final Map<UUID, GameProfile> realGameProfileMap = new ConcurrentHashMap<>();
    private final Pattern usernamePattern = Pattern.compile("^\\w{3,16}$");

    public DisguiseService(Core plugin) {
        this.databaseService = plugin.getDatabaseService();
        this.redisService = plugin.getRedisService();
        this.textureService = plugin.getTextureService();
        refreshEntries();
        subscribeToDisguiseEntriesRefresh();
    }

    public void disguise(ExeUser<?> user, String name, String skin, DisguiseAction action) {
        applyProfile(user, name, skin, action);
    }

    public void undisguise(ExeUser<?> user) {
        applyProfile(user, null, null, DisguiseAction.UNDISGUISE);
    }

    private void applyProfile(ExeUser<?> user, String name, String skin, DisguiseAction action) {
        boolean forceAsync = action != DisguiseAction.UNDISGUISE && textureService.get(name) == null && ServerUtil.isServerThread();

        Executor.schedule(() -> {
            BulkData bulkData = user.getBulkData();
            EntityPlayer entityPlayer = user.getEntityPlayer();
            GameProfile gameProfile = entityPlayer.getProfile();
            TextureEntry textureEntry;
            String newProfileName;

            if (action == DisguiseAction.UNDISGUISE) {
                // If it's an undisguise, we don't care on which thread this is run, only that it's async
                Executor.schedule(() -> redisService.deleteValueFromHash(RedisCache.DISGUISE, user.getUniqueId()))
                        .run(ServerUtil.isServerThread());
                bulkData.setDisguiseModel(null);
                textureEntry = PlayerUtil.getTextureEntry(getRealGameProfile(user));
                newProfileName = user.getRealName();
            } else {
                DisguiseModel disguiseModel = new DisguiseModel(name, skin);
                // If it's a redisguise (switched servers / data in redis already exists) then
                // there's no need to add a duplicate value to the hash
                if (action != DisguiseAction.REDISGUISE) {
                    // DISGUISE action is always run on an async thread, but just to make sure
                    Executor.schedule(() -> redisService.addValueToHash(RedisCache.DISGUISE, user.getUniqueId(), disguiseModel))
                            .run(ServerUtil.isServerThread());
                }
                bulkData.setDisguiseModel(disguiseModel);
                saveRealGameProfile(user);
                textureEntry = textureService.getOrFetch(skin);
                newProfileName = name;
            }
            if (textureEntry != null) {
                PlayerUtil.updateTextures(gameProfile, textureEntry);
            }
            PlayerUtil.setGameProfileName(gameProfile, newProfileName);
            user.instantNametagRefresh();

            Location location = user.getLocation();
            byte compressedYaw = MathUtil.getCompressedAngle(location.getYaw());
            byte compressedPitch = MathUtil.getCompressedAngle(location.getPitch());

            PacketPlayOutPlayerInfo removeTabPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
            PacketPlayOutPlayerInfo addTabPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
            PacketPlayOutEntity.PacketPlayOutEntityLook bodyPositionPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(entityPlayer.getId(), compressedYaw, compressedPitch, true);
            PacketPlayOutEntityHeadRotation headPositionPacket = new PacketPlayOutEntityHeadRotation(entityPlayer, compressedYaw);

            for (EntityPlayer player : MinecraftServer.getServer().getPlayerList().players) {
                if (player.playerConnection == null) continue;
                player.playerConnection.sendPacket(removeTabPacket);
                player.playerConnection.sendPacket(addTabPacket);
            }
            EntityTrackerEntry trackerEntry = ((WorldServer) user.getEntityPlayer().world).getTracker().trackedEntities.get(entityPlayer.getId());

            if (trackerEntry != null) {
                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityPlayer.getId());
                PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(entityPlayer);

                trackerEntry.broadcast(destroyPacket);
                trackerEntry.broadcast(spawnPacket);
                trackerEntry.broadcast(bodyPositionPacket);
                trackerEntry.broadcast(headPositionPacket);
            }
        }).run(forceAsync);
    }

    public boolean canAccess(ExeUser<?> user) {
        // Devs, Admins, Owners and from Elite to Partner
        return user.isEqualOrAbove(Rank.DEVELOPER)
                || (user.isEqualOrAbove(Rank.ELITE) && !user.isAbove(Rank.PARTNER))
                || user.hasPermission("perk.disguise");
    }

    private GameProfile getRealGameProfile(ExeUser<?> user) {
        GameProfile gameProfile = realGameProfileMap.get(user.getUniqueId());

        if (gameProfile == null) {
            user.sendMessage(CC.RED + "An issue occurred while fetching your real game profile, please contact a developer.");
            gameProfile = user.getEntityPlayer().getProfile();
        }
        return gameProfile;
    }

    private void saveRealGameProfile(ExeUser<?> user) {
        // We need to store it just once because real game profile
        // properties are always the same and don't need refreshing
        if (realGameProfileMap.get(user.getUniqueId()) != null) return;

        GameProfile gameProfile = user.getEntityPlayer().getProfile();
        GameProfile newProfile = new GameProfile(gameProfile.getId(), gameProfile.getName());
        newProfile.getProperties().putAll(gameProfile.getProperties());
        realGameProfileMap.put(user.getUniqueId(), newProfile);
    }

    public void addValue(DisguiseEntryType entryType, String value) {
        checkIfCollectionIsValid(entryType.getCollection());
        Document document = new Document(DatabaseUtil.PRIMARY_KEY, value);
        databaseService.update(entryType.getCollection(), document, document).run();
        sendDisguiseEntriesRefresh();
    }

    public void removeValue(DisguiseEntryType entryType, String value) {
        checkIfCollectionIsValid(entryType.getCollection());
        databaseService.delete(entryType.getCollection(), Filters.eq(DatabaseUtil.PRIMARY_KEY, value)).run();
        sendDisguiseEntriesRefresh();
    }

    public boolean hasValue(DisguiseEntryType entryType, String value) {
        return getSetByType(entryType)
                .stream()
                .anyMatch(element -> element.getValue().equalsIgnoreCase(value));
    }

    public String getRandomValue(DisguiseEntryType entryType) {
        Collection<String> namesInUse = entryType == DisguiseEntryType.NAME ? getNamesInUse() : null;
        List<String> possibleNames = getSetByType(entryType)
                .stream()
                .map(DisguiseEntry::getValue)
                .filter(value -> entryType != DisguiseEntryType.NAME || namesInUse
                        .stream()
                        .noneMatch(name -> name.equalsIgnoreCase(value))
                ).collect(Collectors.toList());
        if (possibleNames.isEmpty()) return null;
        return possibleNames.get(ThreadLocalRandom.current().nextInt(possibleNames.size()));
    }

    private Set<DisguiseEntry> getSetByType(DisguiseEntryType entryType) {
        if (entryType == DisguiseEntryType.NAME) {
            return disguiseNames;
        }
        if (entryType == DisguiseEntryType.SKIN) {
            return disguiseSkins;
        }
        throw new IllegalArgumentException("You can only get set by NAME or SKIN");
    }

    public boolean isValidMinecraftName(String name) {
        return usernamePattern.matcher(name).matches();
    }

    private Collection<String> getNamesInUse() {
        return redisService.getHashValues(RedisCache.DISGUISE);
    }

    private void sendDisguiseEntriesRefresh() {
        redisService.getPublisher().sendDisguiseEntriesRefresh();
    }

    private void subscribeToDisguiseEntriesRefresh() {
        redisService.subscribe(RedisMessage.DISGUISE_ENTRIES_REFRESH, Object.class, object -> refreshEntries());
    }

    private void refreshEntries() {
        CollectionUtil.replace(disguiseNames, databaseService.findAll(DatabaseCollection.DISGUISE_NAMES, DisguiseEntry.class).run());
        CollectionUtil.replace(disguiseSkins, databaseService.findAll(DatabaseCollection.DISGUISE_SKINS, DisguiseEntry.class).run());
    }

    private void checkIfCollectionIsValid(DatabaseCollection collection) {
        if (collection != DatabaseCollection.DISGUISE_NAMES && collection != DatabaseCollection.DISGUISE_SKINS) {
            throw new IllegalArgumentException("You can only add values to DISGUISE_NAMES and DISGUISE_SKINS collections");
        }
    }
}
