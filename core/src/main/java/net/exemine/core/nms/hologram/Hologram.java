package net.exemine.core.nms.hologram;

import lombok.Getter;
import lombok.Setter;
import net.exemine.core.Core;
import net.exemine.core.user.base.ExeUser;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Hologram {

    private static final Map<Integer, Hologram> HOLOGRAMS = new HashMap<>();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    public static final double SEPARATOR_HEIGHT = 0.3;

    private final int entityId;

    private String displayName;
    private Location location;

    private final List<Hologram> below = new ArrayList<>();
    private final List<Hologram> above = new ArrayList<>();

    private Hologram(String text, Location location, boolean add) {
        this.entityId = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        this.displayName = text;
        this.location = location;

        if (add) {
            HOLOGRAMS.put(entityId, this);
        }
    }

    public Hologram(String text, Location location) {
        this(text, location, true);
    }

    public Hologram(String text) {
        this(text, new Location(Bukkit.getWorlds().get(0), 0, 10, 0), true);
    }

    public void spawn(ExeUser<?> user) {
        if (displayName.isEmpty() || !user.inWorld(location.getWorld())) return;

        EXECUTOR.execute(() -> {
            if (user.is1_8()) {
                EntityArmorStand armorStand = new EntityArmorStand(((CraftWorld) user.getWorld()).getHandle());

                armorStand.d(entityId);
                armorStand.setLocation(location.getX(), location.getY() - 2.25, location.getZ(), 0, 0);
                armorStand.setInvisible(true);
                armorStand.setCustomNameVisible(true);
                armorStand.setCustomName(displayName);
                armorStand.setGravity(false);

                user.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
            } else {
                int x = MathHelper.floor(location.getX() * 32.0D);
                int z = MathHelper.floor(location.getZ() * 32.0D);

                PacketPlayOutSpawnEntityLiving entity = new PacketPlayOutSpawnEntityLiving();
                entity.id = entityId;
                entity.type = 100; // Horse

                DataWatcher watcher = new DataWatcher(null);
                watcher.a(0, (byte) 0);
                watcher.a(1, (short) 300);
                watcher.a(2, displayName);
                watcher.a(3, (byte) 1);
                watcher.a(10, displayName);
                watcher.a(11, (byte) 1);
                watcher.a(12, -1700000);
                entity.l = watcher;

                PacketPlayOutSpawnEntity floatingEntity = new PacketPlayOutSpawnEntity();
                floatingEntity.a = entityId + 1;
                floatingEntity.c = MathHelper.floor((location.getY() + 54.55) * 32.0D) - 1;
                floatingEntity.b = x;
                floatingEntity.d = z;
                floatingEntity.j = 66; // Skull

                PacketPlayOutAttachEntity attach = new PacketPlayOutAttachEntity(entityId, entityId + 1);

                user.sendPacket(entity);
                user.sendPacket(floatingEntity);
                user.sendPacket(attach);

                user.sendPacket(new PacketPlayOutAttachEntity(entityId, entityId + 1));
            }
        });
    }

    public void spawn() {
        Core.get().getUserService().getOnlineUsers().forEach(this::spawn);
    }

    public void rename(String displayName) {
        if (getDisplayName().equals(displayName)) return;

        EXECUTOR.execute(() -> {
            setDisplayName(displayName);

            DataWatcher watcher = new DataWatcher(null);
            watcher.a(2, displayName);
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(entityId, watcher, true);

            Core.get().getUserService().getOnlineUsers().forEach(user -> user.sendPacket(packet));
        });
    }

    public Hologram addLineBelow(String text, ExeUser<?> user) {
        Hologram hologram = new Hologram(text, (below.isEmpty() ? location : below.get(below.size() - 1).getLocation()).clone().add(0, -SEPARATOR_HEIGHT, 0), false);
        below.add(hologram);

        if (user == null) {
            Core.get().getUserService().getOnlineUsers().forEach(hologram::spawn);
        } else {
            hologram.spawn(user);
        }
        return this;
    }

    public Hologram addLineBelow(String text) {
        addLineBelow(text, null);
        return this;
    }

    public Hologram addLineAbove(String text, ExeUser<?> user) {
        Hologram hologram = new Hologram(text, (above.isEmpty() ? location : above.get(above.size() - 1).getLocation()).clone().add(0, SEPARATOR_HEIGHT, 0), false);
        above.add(hologram);

        if (user == null) {
            Core.get().getUserService().getOnlineUsers().forEach(hologram::spawn);
        } else {
            hologram.spawn(user);
        }
        return this;
    }

    public Hologram addLineAbove(String text) {
        addLineAbove(text, null);
        return this;
    }

    public void removeLineBelow(int line) {
        removeLineBelow(line, null);
    }

    public void removeLineBelow(int line, ExeUser<?> user) {
        try {
            below.remove(line).destroy(user);
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    public void removeLineAbove(int line) {
        removeLineAbove(line, null);
    }

    public void removeLineAbove(int line, ExeUser<?> user) {
        try {
            above.remove(line).destroy(user);
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    public Hologram getLineBelow(int index) {
        try {
            return below.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public Hologram getLineAbove(int index) {
        try {
            return above.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void destroy() {
        destroy(true);
    }

    public void destroy(boolean remove) {
        Core.get().getUserService().getOnlineUsers().forEach(this::destroy);

        if (remove) {
            HOLOGRAMS.remove(entityId);
        }
    }

    public void destroy(ExeUser<?> user) {
        EXECUTOR.execute(() -> {
            above.forEach(above -> user.sendPacket(getDestroyPacket(above)));
            below.forEach(below -> user.sendPacket(getDestroyPacket(below)));

            user.sendPacket(getDestroyPacket(this));
        });
    }

    private PacketPlayOutEntityDestroy getDestroyPacket(Hologram hologram) {
        return new PacketPlayOutEntityDestroy(hologram.getEntityId(), hologram.getEntityId() + 1);
    }

    @Nullable
    public static Hologram get(int entityID) {
        return HOLOGRAMS.get(entityID);
    }

    public static Collection<Hologram> getHolograms() {
        return HOLOGRAMS.values();
    }
}