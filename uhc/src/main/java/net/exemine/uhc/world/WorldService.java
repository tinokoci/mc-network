package net.exemine.uhc.world;

import com.execets.spigot.ExeSpigot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.util.Executor;
import net.exemine.api.util.FileUtil;
import net.exemine.api.util.LogUtil;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.ServerUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.logger.CombatLoggerEntity;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@Getter
@Setter
public class WorldService {

    private final UHC plugin;
    private final ConfigFile configFile;
    private final BorderService borderService;

    private int animalSpawnRate = 2;
    private boolean worldsUsed;

    public static final String LOBBY_WORLD_NAME = "uhc_lobby";
    public static final String PRACTICE_WORLD_NAME = "uhc_practice";
    public static final String UHC_WORLD_NAME = "uhc_world";
    public static final String NETHER_WORLD_NAME = "uhc_nether";

    public WorldService(UHC plugin, BorderService borderService) {
        this.plugin = plugin;
        this.configFile = plugin.getConfigFile();
        this.borderService = borderService;
        this.worldsUsed = configFile.getBoolean("worlds_used");

        loadLobbyWorld();
        loadPracticeWorld();

        LogUtil.info(worldsUsed
                ? CC.GREEN + "Fetching new worlds from the generators..."
                : CC.RED + "Not fetching new worlds since a match wasn't played.");
        if (worldsUsed) {
            deleteWorlds();
            Executor.schedule(this::copyWorlds).runSyncLater(1000L);
            setWorldsUsed(false);
        }
        Executor.schedule(() -> {
            loadUHCWorld();
            loadNetherWorld();

            borderService.shrinkBorder(borderService.getCurrentRadius());
            plugin.getGameService().setState(GameState.LOBBY);

            clearEntities(ClearType.MEDIUM);
        }).runSyncLater(5000L);

        ExeSpigot.INSTANCE.addMovementHandler(new WorldMoveHandler(this, plugin.getUserService()));
    }

    private void loadLobbyWorld() {
        World world = Bukkit.createWorld(new WorldCreator(LOBBY_WORLD_NAME));
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setTime(6000L);
        world.setStorm(false);
        world.setThundering(false);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setPVP(false);
    }

    private void loadPracticeWorld() {
        World world = Bukkit.createWorld(new WorldCreator(PRACTICE_WORLD_NAME));
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setStorm(false);
        world.setThundering(false);
        world.setDifficulty(Difficulty.HARD);
    }

    private void loadUHCWorld() {
        World world = Bukkit.createWorld(new WorldCreator(UHC_WORLD_NAME));
        world.setSpawnLocation(0, world.getHighestBlockYAt(0, 0) + 20, 0);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setTime(6000L);
        world.setStorm(false);
        world.setThundering(false);
        world.setDifficulty(Difficulty.HARD);
        world.setPVP(false);

        ServerUtil.performCommand("wb shape square");
        ServerUtil.performCommand("wb " + UHC_WORLD_NAME + " set " + borderService.getCurrentRadius().getValue() + " 0 0");
    }

    private void loadNetherWorld() {
        World world = Bukkit.createWorld(new WorldCreator(NETHER_WORLD_NAME).environment(World.Environment.NETHER));
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setDifficulty(Difficulty.HARD);
        world.setPVP(false);

        ServerUtil.performCommand("wb shape square");
        ServerUtil.performCommand("wb " + NETHER_WORLD_NAME + " set 350 0 0");
    }

    public void updateCustomWorldBorder(int radius) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + UHC_WORLD_NAME + " set " + radius + " 0 0");
    }

    private void deleteWorlds() {
        if (getUhcWorld() != null) Bukkit.unloadWorld(getUhcWorld(), false);
        if (getNetherWorld() != null) Bukkit.unloadWorld(getNetherWorld(), false);

        FileUtil.deleteDirectory(new File(Bukkit.getWorldContainer(), UHC_WORLD_NAME));
        LogUtil.info("Deleted world " + UHC_WORLD_NAME);
        FileUtil.deleteDirectory(new File(Bukkit.getWorldContainer(), NETHER_WORLD_NAME));
        LogUtil.info("Deleted world " + NETHER_WORLD_NAME);
    }

    private void copyWorlds() {
        if (!copyWorld(UHC_WORLD_NAME, false)) {
            copyWorld(UHC_WORLD_NAME, true);
        }
        LogUtil.info("Copied world " + UHC_WORLD_NAME);
        if (!copyWorld(NETHER_WORLD_NAME, false)) {
            copyWorld(NETHER_WORLD_NAME, true);
        }
        LogUtil.info("Copied world " + NETHER_WORLD_NAME);
    }

    private boolean copyWorld(String worldName, boolean copyBackup) {
        File directory = new File(plugin.getConfigFile().getString(
                worldName.replace("uhc_", "") + "_generator_directory", "") +
                (copyBackup ? "/backup" : "/pool"));

        if (!directory.exists() || FileUtil.isEmpty(directory)) {
            return false;
        }
        // Find world on the generator instance
        File file = Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .filter(world -> world.getName().startsWith("uhc"))
                .min(Comparator.comparingInt(world -> Integer.parseInt(world.getName().split("-")[1])))
                .orElse(null);
        if (file == null) {
            return false;
        }
        try {
            // Grab our world
            FileUtil.copyFolder(file, new File(Bukkit.getWorldContainer(), worldName));
            // Don't delete backups
            if (!copyBackup) {
                // Delete the original world on the generator instance
                Executor.schedule(() -> FileUtil.deleteDirectory(file)).runSyncLater(3000L);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setWorldsUsed(boolean worldsUsed) {
        this.worldsUsed = worldsUsed;
        configFile.set("worlds_used", worldsUsed);
        configFile.save();
    }

    public boolean isWorld(World originalWorld, World... worldsToMatch) {
        return Arrays.stream(worldsToMatch).anyMatch(world -> world == originalWorld);
    }

    public World getLobbyWorld() {
        return Bukkit.getWorld(LOBBY_WORLD_NAME);
    }

    public World getPracticeWorld() {
        return Bukkit.getWorld(PRACTICE_WORLD_NAME);
    }

    public World getUhcWorld() {
        return Bukkit.getWorld(UHC_WORLD_NAME);
    }

    public World getNetherWorld() {
        return Bukkit.getWorld(NETHER_WORLD_NAME);
    }

    public int clearEntities(ClearType type) {
        AtomicInteger removed = new AtomicInteger();
        plugin.getServer().getWorlds().forEach(world -> world.getEntities().stream()
                .filter(type.getFilter())
                .forEach(entity -> {
                    entity.remove();
                    removed.getAndIncrement();
                }));
        return removed.get();
    }

    @Getter
    @RequiredArgsConstructor
    public enum ClearType {
        HARD(entity -> !(entity instanceof Player ||
                entity instanceof CombatLoggerEntity)),
        MEDIUM(entity -> !(entity instanceof Player ||
                entity instanceof CombatLoggerEntity ||
                entity instanceof Cow ||
                entity instanceof Chicken ||
                entity instanceof Sheep)),
        SOFT(entity -> entity instanceof Item);

        public static ClearType getByName(String name) {
            return Arrays.stream(values())
                    .filter(type -> type.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        }

        private final Predicate<Entity> filter;
    }
}
