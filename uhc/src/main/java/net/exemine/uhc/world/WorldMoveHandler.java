package net.exemine.uhc.world;

import com.execets.spigot.handler.MovementHandler;
import lombok.RequiredArgsConstructor;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.SpawnerCreature;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.util.Random;

@RequiredArgsConstructor
public class WorldMoveHandler implements MovementHandler {

    private final WorldService worldService;
    private final UHCUserService userService;

    @Override
    public void handleUpdateLocation(Player player, Location from, Location to, PacketPlayInFlying packet) {
        UHCUser user = userService.get(player);

        if (!user.isPlaying() || !to.getWorld().equals(worldService.getUhcWorld()) || to.getChunk().isTouched()) {
            return;
        }
        for (int i = 0; i < worldService.getAnimalSpawnRate(); i++) {
            spawnGroup(to);
        }
        to.getChunk().setTouched(true);
    }

    private void spawnGroup(Location location) {
        SpawnerCreature.a(
                ((CraftWorld) location.getWorld()).getHandle(),
                BiomeBase.getBiome(location.getBlock().getBiome().getId()),
                location.getBlockX() + 8,
                location.getBlockZ() + 8,
                16,
                16,
                new Random());
    }
}
