package net.exemine.uhc.logger;

import lombok.Getter;
import net.exemine.api.util.Executor;
import net.exemine.uhc.UHC;
import net.exemine.uhc.config.option.NumberOption;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CombatLoggerService {

    @Getter
    private final Map<UUID, CombatLogger> loggers = new HashMap<>();

    public CombatLoggerService(UHC plugin) {
        EntityTypes.registerEntity(CombatLoggerEntity.class, "CraftVillager", EntityType.VILLAGER.getTypeId());
        Bukkit.getPluginManager().registerEvents(new CombatLoggerListener(this, plugin.getUserService()), plugin);
        schedule();
    }

    private void schedule() {
        Executor.schedule(() -> {
            if (loggers.isEmpty()) return;

            Set<UUID> toRemove = new HashSet<>();

            loggers.forEach((uuid, entry) -> {
                if (System.currentTimeMillis() - entry.getTimestamp() > NumberOption.RELOG_TIME.getMinutesInMillis()) {
                    CombatLoggerEntity entity = entry.getEntity();

                    if (entity != null) {
                        Executor.schedule(() -> entity.die(null)).runSync();
                    }
                    toRemove.add(uuid);
                }
            });
            toRemove.forEach(loggers::remove);
        }).runAsyncTimer(0, 1000L);
    }
}