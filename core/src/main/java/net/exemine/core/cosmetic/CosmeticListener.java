package net.exemine.core.cosmetic;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.Executor;
import net.exemine.core.cosmetic.bow.BowTrail;
import net.exemine.core.cosmetic.rod.RodTrail;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.particle.ParticleEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CosmeticListener implements Listener {

    private final UserService<CoreUser, CoreData> userService;
    private final Map<Entity, ParticleEffect> users = new HashMap<>();

    public CosmeticListener(UserService<CoreUser, CoreData> userService) {
        this.userService = userService;
        spawnParticles();
    }

    private void spawnParticles() {
        Executor.schedule(() -> {
            if (users.isEmpty()) return;

            Iterator<Map.Entry<Entity, ParticleEffect>> iterator = users.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Entity, ParticleEffect> entry = iterator.next();
                Entity item = entry.getKey();

                if (item == null || item.isDead() || item.isOnGround()) {
                    iterator.remove();
                    return;
                }
                entry.getValue().display(0, 0, 0, 0, 1, item.getLocation(), 256);
            }
        }).runAsyncTimer(0L, 75L);
    }

    @EventHandler
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        Projectile entity = event.getEntity();
        if (!(entity.getShooter() instanceof Player)) return;

        CoreUser user = userService.get((Player) entity.getShooter());

        if (event.getEntityType() == EntityType.FISHING_HOOK) {
            RodTrail trail = user.getRodTrail();

            if (trail != null) {
                users.put(entity, trail.getEffect());
            }
        } else if (event.getEntityType() == EntityType.ARROW) {
            BowTrail trail = user.getBowTrail();

            if (trail != null) {
                users.put(entity, trail.getEffect());
            }
        }
    }

    @EventHandler
    public void onEntityHook(PlayerFishEvent event) {
        users.remove(event.getHook());
    }
}