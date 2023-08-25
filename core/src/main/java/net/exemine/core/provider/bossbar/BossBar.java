package net.exemine.core.provider.bossbar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.core.user.base.ExeUser;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class BossBar<T extends ExeUser<?>> {

    private final T user;

    private final List<BossBarElement> elements = new ArrayList<>();
    private int elementIndex = 0;

    @Setter
    private int secondsPerElement;
    private long lastElementChangeTime;

    private EntityEnderDragon dragon;

    public void add(String text, float percentage) {
        elements.add(new BossBarElement(text, percentage * 200f)); // 200f is max dragon health (bar length)
    }

    public void spawn() {
        getElement().ifPresentOrElse(element -> {
            if (!inRange() || !user.getCoreData().isBossBar()) {
                destroyEntity();
            }
            if (dragon != null) {
                DataWatcher watcher = new DataWatcher(dragon);
                watcher.a(2, element.getText());
                watcher.a(6, element.getPercentage());
                watcher.a(10, element.getText());

                PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(dragon.getId(), watcher, true);
                user.sendPacket(packet);
                return;
            }
            Location location = user.getLocation();
            dragon = new EntityEnderDragon(((CraftWorld) user.getWorld()).getHandle());

            dragon.setCustomName(element.getText());
            dragon.setLocation(location.getX(), location.getY() - 200, location.getZ(), 0f, 0f);
            dragon.setHealth(element.getPercentage());
            dragon.setInvisible(true);

            user.sendPacket(new PacketPlayOutSpawnEntityLiving(dragon));
        }, () -> dragon = null);
    }

    public void despawn() {
        if (getElement().isEmpty() && dragon != null) {
            destroyEntity();
        }
    }

    private void destroyEntity() {
        if (dragon == null) return;
        user.sendPacket(new PacketPlayOutEntityDestroy(dragon.getId()));
        dragon = null;
    }

    private boolean inRange() {
        if (dragon == null) return false;

        CraftEntity bukkitEntity = dragon.getBukkitEntity();
        if (!user.inWorld(bukkitEntity.getWorld())) return false;

        return user.getLocation().distance(bukkitEntity.getLocation()) < 500;
    }

    private Optional<BossBarElement> getElement() {
        if (elements.isEmpty()) return Optional.empty();

        boolean shouldChangeElement = System.currentTimeMillis() - lastElementChangeTime > secondsPerElement * 1000L;
        if (shouldChangeElement) {
            elementIndex++;
            lastElementChangeTime = System.currentTimeMillis();
        }
        if (elementIndex > elements.size() - 1) {
            elementIndex = 0;
        }
        return Optional.of(elements.get(elementIndex));
    }

    public void clear() {
        elements.clear();
    }

    @RequiredArgsConstructor
    @Getter
    private static class BossBarElement {

        private final String text;
        private final float percentage;
    }
}
