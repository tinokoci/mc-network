package net.exemine.uhc.logger;

import com.mojang.authlib.GameProfile;
import net.exemine.api.util.string.CC;
import net.exemine.uhc.UHC;
import net.exemine.uhc.logger.event.CombatLoggerDeathEvent;
import net.exemine.uhc.user.UHCUser;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityAgeable;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CombatLoggerEntity extends EntityVillager {

    private static final Function<Double, Double> DAMAGE_FUNCTION = f1 -> 0.0;

    private final UUID uuid;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final Location location;

    public UUID getUniqueId() {
        return uuid;
    }

    public CombatLoggerEntity(World world, Location location, UHCUser user, ItemStack[] inventory, ItemStack[] armor) {
        super(((CraftWorld) world).getHandle());

        this.inventory = inventory;
        this.armor = armor;
        this.location = location;
        lastDamager = user.getEntityPlayer().lastDamager;
        uuid = user.getUniqueId();
        getBukkitEntity().setMaxHealth(user.getMaxHealth());
        getBukkitEntity().setHealth(user.getHealth());
        setInvisible(false);
        setCustomName(user.getColoredDisplayName());
        setCustomNameVisible(true);
        setPositionRotation(user.getLocation().getX(), user.getLocation().getY(), user.getLocation().getZ(), location.getYaw(), location.getPitch());
        fallDistance = user.getFallDistance();
        this.world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        final EntityTracker entityTracker = ((CraftWorld) world).getHandle().getTracker();
        entityTracker.untrackEntity(this);
        entityTracker.track(this);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable entityAgeable) {
        return null;
    }

    @Override
    public void move(double d0, double d1, double d2) {
        super.move(0, d1, 0);
    }

    public void b(int i) {
    }

    @Override
    public void dropDeathLoot(boolean flag, int i) {
    }

    public Entity findTarget() {
        return null;
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float amount) {
        if (!UHC.get().getGameService().isPvP()) return false;

        setPosition(this.locX, this.locY, this.locZ);
        EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent(this, damageSource, amount, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, DAMAGE_FUNCTION::apply, DAMAGE_FUNCTION::apply, DAMAGE_FUNCTION::apply, DAMAGE_FUNCTION::apply, DAMAGE_FUNCTION::apply, DAMAGE_FUNCTION::apply);
        if (event.isCancelled()) return false;

        return super.damageEntity(damageSource, amount);
    }

    @Override
    public boolean a(EntityHuman entityHuman) {
        return false;
    }

    @Override
    public void h() {
        super.h();
    }

    @Override
    public void collide(Entity entity) {
    }

    @Override
    public void die(DamageSource damageSource) {
        List<ItemStack> drops = new ArrayList<>();
        drops.addAll(Arrays.stream(inventory)
                .filter(item -> item != null && item.getType() != Material.AIR)
                .collect(Collectors.toList()));
        drops.addAll(Arrays.stream(armor)
                .filter(item -> item != null && item.getType() != Material.AIR)
                .collect(Collectors.toList()));

        UHCUser user = UHC.get().getUserService().retrieve(uuid);

        if (damageSource != null && damageSource.getEntity() instanceof EntityPlayer) {
            user.getGameInfo().setLoggerKillerUuid(damageSource.getEntity().getUniqueID());
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        WorldServer worldServer = world.getWorld().getHandle();
        EntityPlayer entityPlayer = new EntityPlayer(((CraftServer) Bukkit.getServer()).getServer(), worldServer, new GameProfile(uuid, offlinePlayer.getName()), new PlayerInteractManager(worldServer));
        entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        String deathMessage = damageSource != null
                ? "<victim> " + CC.DARK_GRAY + "(Combat Logger) " + CC.GRAY + "was slain by <killer>."
                : "<victim> " + CC.GRAY + "has disconnected for too long and has been disqualified.";
        user.setCustomDeathMessage(deathMessage);
        CraftEventFactory.callPlayerDeathEvent(entityPlayer, drops, deathMessage, false);

        if (damageSource != null) {
            super.die(damageSource);
        }
        CombatLoggerDeathEvent loggerDeathEvent = new CombatLoggerDeathEvent(this);
        Bukkit.getPluginManager().callEvent(loggerDeathEvent);

        ((WorldServer) this.world).getTracker().untrackEntity(this);
        getBukkitEntity().remove();
    }

    @Override
    public CraftLivingEntity getBukkitEntity() {
        return (CraftLivingEntity) super.getBukkitEntity();
    }
}