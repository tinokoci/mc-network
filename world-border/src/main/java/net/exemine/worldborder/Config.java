package net.exemine.worldborder;

import net.exemine.worldborder.tasks.BorderCheckTask;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config
{
    // private stuff used within this class
    private static WorldBorder plugin;
    private static FileConfiguration cfg = null;
    private static Logger wbLog = null;
    public static DecimalFormat coord = new DecimalFormat("0.0");
    private static int borderTask = -1;
    static WorldFillTask fillTask;
    private static Runtime rt = Runtime.getRuntime();

    // actual configuration values which can be changed
    private static boolean shapeRound = true;
    private static Map<String, BorderData> borders = Collections.synchronizedMap(new LinkedHashMap<String, BorderData>());
    private static Set<String> bypassPlayers = Collections.synchronizedSet(new LinkedHashSet<String>());
    private static String message;		// raw message without color code formatting
    private static String messageFmt;	// message with color code formatting ("&" changed to funky sort-of-double-dollar-sign for legitimate color/formatting codes)
    private static boolean DEBUG = false;
    private static double knockBack = 3.0;
    private static int timerTicks = 4;
    private static boolean whooshEffect = false;
    private static boolean portalRedirection = true;
    private static boolean dynmapEnable = true;
    private static String dynmapMessage;
    private static int remountDelayTicks = 0;
    private static boolean killPlayer = false;
    private static boolean denyEnderpearl = false;
    private static int fillAutosaveFrequency = 30;
    private static int fillMemoryTolerance = 500;

    // for monitoring plugin efficiency
//	public static long timeUsed = 0;

    public static long Now()
    {
        return System.currentTimeMillis();
    }

    private static void setBorder(String world, BorderData border)
    {
        borders.put(world, border);
        log("Border set. " + BorderDescription(world));
        save(true);
    }

    static void setBorder(String world, int radiusX, int radiusZ, double x, double z)
    {
        BorderData old = Border(world);
        Boolean oldShape = (old == null) ? null : old.getShape();
        boolean oldWrap = (old != null) && old.getWrapping();
        setBorder(world, new BorderData(x, z, radiusX, radiusZ, oldShape, oldWrap));
    }


    static String BorderDescription(String world)
    {
        BorderData border = borders.get(world);
        if (border == null)
            return "No border was found for the world \"" + world + "\".";
        else
            return "World \"" + world + "\" has border " + border.toString();
    }

    public static BorderData Border(String world)
    {
        return borders.get(world);
    }

    private static void updateMessage(String msg)
    {
        message = msg;
        messageFmt = replaceAmpColors(msg);
        // message cleaned of formatting codes
    }

    public static String Message()
    {
        return messageFmt;
    }

    static void setShape(boolean round)
    {
        shapeRound = round;
        log("Set default border shape to " + (ShapeName()) + ".");
        save(true);
    }

    public static boolean ShapeRound()
    {
        return shapeRound;
    }

    static String ShapeName()
    {
        return ShapeName(shapeRound);
    }
    static String ShapeName(boolean round)
    {
        return round ? "elliptic/round" : "rectangular/square";
    }

    static void setDebug(boolean debugMode)
    {
        DEBUG = debugMode;
        log("Debug mode " + (DEBUG ? "enabled" : "disabled") + ".");
        save(true);
    }

    public static boolean Debug()
    {
        return DEBUG;
    }

    private static boolean whooshEffect()
    {
        return whooshEffect;
    }

    public static void showWhooshEffect(Location loc)
    {
        if (!whooshEffect())
            return;

        World world = loc.getWorld();
        world.playEffect(loc, Effect.ENDER_SIGNAL, 0);
        world.playEffect(loc, Effect.ENDER_SIGNAL, 0);
        world.playEffect(loc, Effect.SMOKE, 4);
        world.playEffect(loc, Effect.SMOKE, 4);
        world.playEffect(loc, Effect.SMOKE, 4);
        world.playEffect(loc, Effect.GHAST_SHOOT, 0);
    }

    public static boolean getIfPlayerKill()
    {
        return killPlayer;
    }

    static boolean getDenyEnderpearl()
    {
        return denyEnderpearl;
    }

    static boolean portalRedirection()
    {
        return portalRedirection;
    }

    public static double KnockBack()
    {
        return knockBack;
    }

    static int FillAutosaveFrequency()
    {
        return fillAutosaveFrequency;
    }


    public static boolean isPlayerBypassing(String player)
    {
        return bypassPlayers.contains(player.toLowerCase());
    }


    static boolean isBorderTimerRunning()
    {
        if (borderTask == -1) return false;
        return (plugin.getServer().getScheduler().isQueued(borderTask) || plugin.getServer().getScheduler().isCurrentlyRunning(borderTask));
    }

     static void StartBorderTimer()
    {
        StopBorderTimer();

        borderTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new BorderCheckTask(), timerTicks, timerTicks);

        if (borderTask == -1)
            logWarn("Failed to start timed border-checking task! This will prevent the plugin from working. Try restarting Bukkit.");

        logConfig("Border-checking timed task started.");
    }

     static void StopBorderTimer()
    {
        if (borderTask == -1) return;

        plugin.getServer().getScheduler().cancelTask(borderTask);
        borderTask = -1;
        logConfig("Border-checking timed task stopped.");
    }

     static void StopFillTask()
    {
        if (fillTask != null && fillTask.valid())
            fillTask.cancel();
    }

     static void StoreFillTask()
    {
        save(false, true);
    }
     static void UnStoreFillTask()
    {
        save(false);
    }

     private static void RestoreFillTask(String world, int fillDistance, int chunksPerRun, int tickFrequency, int x, int z, int length, int total, boolean forceLoad)
    {
        fillTask = new WorldFillTask(plugin.getServer(), null, world, fillDistance, chunksPerRun, tickFrequency, forceLoad);
        if (fillTask.valid())
        {
            fillTask.continueProgress(x, z, length, total);
            int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, fillTask, 20, tickFrequency);
            fillTask.setTaskID(task);
        }
    }

    static int AvailableMemory()
    {
        return (int)((rt.maxMemory() - rt.totalMemory() + rt.freeMemory()) / 1048576);  // 1024*1024 = 1048576 (bytes in 1 MB)
    }

    static boolean AvailableMemoryTooLow()
    {
        return AvailableMemory() < fillMemoryTolerance;
    }


    static boolean HasPermission(Player player, String request)
    {
        return !HasPermission(player, request, true);
    }
    private static boolean HasPermission(Player player, String request, boolean notify)
    {
        if (player == null)				// console, always permitted
            return true;

        if (player.hasPermission("worldborder." + request))	// built-in Bukkit superperms
            return true;

        if (notify)
            player.sendMessage("You do not have sufficient permissions.");

        return false;
    }


     private static String replaceAmpColors(String message)
    {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    // adapted from code posted by Sleaker
     private static String stripAmpColors(String message)
    {
        return message.replaceAll("(?i)&([a-fk-or0-9])", "");
    }


     private static void log(Level lvl, String text)
    {
        wbLog.log(lvl, text);
    }
     public static void log(String text)
    {
        log(Level.INFO, text);
    }
     public static void logWarn(String text)
    {
        log(Level.WARNING, text);
    }
     private static void logConfig(String text)
    {
        log(Level.INFO, "[CONFIG] " + text);
    }


    private static final int currentCfgVersion = 10;

     static void load(WorldBorder master)
    {	// load config from file
        plugin = master;
        wbLog = plugin.getLogger();

        plugin.reloadConfig();
        cfg = plugin.getConfig();

        int cfgVersion = cfg.getInt("cfg-version", currentCfgVersion);

        String msg = cfg.getString("message");
        shapeRound = cfg.getBoolean("round-border", true);
        DEBUG = cfg.getBoolean("debug-mode", false);
        whooshEffect = cfg.getBoolean("whoosh-effect", false);
        portalRedirection = cfg.getBoolean("portal-redirection", true);
        knockBack = cfg.getDouble("knock-back-dist", 3.0);
        timerTicks = cfg.getInt("timer-delay-ticks", 5);
        remountDelayTicks = cfg.getInt("remount-delay-ticks", 0);
        dynmapEnable = cfg.getBoolean("dynmap-border-enabled", true);
        dynmapMessage = cfg.getString("dynmap-border-message", "The border of the world.");
        logConfig("Using " + (ShapeName()) + " border, knockback of " + knockBack + " blocks, and timer delay of " + timerTicks + ".");
        killPlayer = cfg.getBoolean("player-killed-bad-spawn", false);
        denyEnderpearl = cfg.getBoolean("deny-enderpearl", true);
        fillAutosaveFrequency = cfg.getInt("fill-autosave-frequency", 30);
        bypassPlayers = Collections.synchronizedSet(new LinkedHashSet<>(cfg.getStringList("bypass-list")));
        fillMemoryTolerance = cfg.getInt("fill-memory-tolerance", 500);

        StartBorderTimer();

        borders.clear();

        // if empty border message, assume no config
        if (msg == null || msg.isEmpty())
        {	// store defaults
            logConfig("Configuration not present, creating new file.");
            msg = "&cYou have reached the edge of this world.";
            updateMessage(msg);
            save(false);
            return;
        }
        // if loading older config which didn't support color codes in border message, make sure default red color code is added at start of it
        else if (cfgVersion < 8 && !(msg.substring(0, 1).equals("&")))
            updateMessage("&c" + msg);
            // otherwise just set border message
        else
            updateMessage(msg);

        // this option defaulted to false previously, but what it actually does has changed to something that almost everyone should now want by default
        if (cfgVersion < 10)
            denyEnderpearl = true;

        ConfigurationSection worlds = cfg.getConfigurationSection("worlds");
        if (worlds != null)
        {
            Set<String> worldNames = worlds.getKeys(false);

            for (String worldName : worldNames)
            {
                ConfigurationSection bord = worlds.getConfigurationSection(worldName);

                // we're swapping "<" to "." at load since periods denote configuration nodes without a working way to change that, so world names with periods wreak havoc and are thus modified for storage
                if (cfgVersion > 3)
                    worldName = worldName.replace("<", ".");

                // backwards compatibility for config from before elliptical/rectangular borders were supported
                if (bord.isSet("radius") && !bord.isSet("radiusX"))
                {
                    int radius = bord.getInt("radius");
                    bord.set("radiusX", radius);
                    bord.set("radiusZ", radius);
                }

                Boolean overrideShape = (Boolean) bord.get("shape-round");
                boolean wrap = bord.getBoolean("wrapping", false);
                BorderData border = new BorderData(bord.getDouble("x", 0), bord.getDouble("z", 0), bord.getInt("radiusX", 0), bord.getInt("radiusZ", 0), overrideShape, wrap);
                borders.put(worldName, border);
                logConfig(BorderDescription(worldName));
            }
        }

        // if we have an unfinished fill task stored from a previous run, load it up
        ConfigurationSection storedFillTask = cfg.getConfigurationSection("fillTask");
        if (storedFillTask != null)
        {
            String worldName = storedFillTask.getString("world");
            int fillDistance = storedFillTask.getInt("fillDistance", 176);
            int chunksPerRun = storedFillTask.getInt("chunksPerRun", 5);
            int tickFrequency = storedFillTask.getInt("tickFrequency", 20);
            int fillX = storedFillTask.getInt("x", 0);
            int fillZ = storedFillTask.getInt("z", 0);
            int fillLength = storedFillTask.getInt("length", 0);
            int fillTotal = storedFillTask.getInt("total", 0);
            boolean forceLoad = storedFillTask.getBoolean("forceLoad", false);
            RestoreFillTask(worldName, fillDistance, chunksPerRun, tickFrequency, fillX, fillZ, fillLength, fillTotal, forceLoad);
            save(false);
        }

        if (cfgVersion < currentCfgVersion)
            save(false);
    }

     private static void save(boolean logIt)
    {
        save(logIt, false);
    }
    private static void save(boolean logIt, boolean storeFillTask)
    {	// save config to file
        if (cfg == null) return;

        cfg.set("cfg-version", currentCfgVersion);
        cfg.set("message", message);
        cfg.set("round-border", shapeRound);
        cfg.set("debug-mode", DEBUG);
        cfg.set("whoosh-effect", whooshEffect);
        cfg.set("portal-redirection", portalRedirection);
        cfg.set("knock-back-dist", knockBack);
        cfg.set("timer-delay-ticks", timerTicks);
        cfg.set("remount-delay-ticks", remountDelayTicks);
        cfg.set("dynmap-border-enabled", dynmapEnable);
        cfg.set("dynmap-border-message", dynmapMessage);
        cfg.set("player-killed-bad-spawn", killPlayer);
        cfg.set("deny-enderpearl", denyEnderpearl);
        cfg.set("fill-autosave-frequency", fillAutosaveFrequency);
        cfg.set("bypass-list", new ArrayList<>(bypassPlayers));
        cfg.set("fill-memory-tolerance", fillMemoryTolerance);

        cfg.set("worlds", null);
        for (Map.Entry<String, BorderData> stringBorderDataEntry : borders.entrySet())
        {
            String name = ((String) ((Map.Entry) stringBorderDataEntry).getKey()).replace(".", "<");
            BorderData bord = (BorderData) ((Map.Entry) stringBorderDataEntry).getValue();

            cfg.set("worlds." + name + ".x", bord.getX());
            cfg.set("worlds." + name + ".z", bord.getZ());
            cfg.set("worlds." + name + ".radiusX", bord.getRadiusX());
            cfg.set("worlds." + name + ".radiusZ", bord.getRadiusZ());
            cfg.set("worlds." + name + ".wrapping", bord.getWrapping());

            if (bord.getShape() != null)
                cfg.set("worlds." + name + ".shape-round", bord.getShape());
        }

        if (storeFillTask && fillTask != null && fillTask.valid())
        {
            cfg.set("fillTask.world", fillTask.refWorld());
            cfg.set("fillTask.fillDistance", fillTask.refFillDistance());
            cfg.set("fillTask.chunksPerRun", fillTask.refChunksPerRun());
            cfg.set("fillTask.tickFrequency", fillTask.refTickFrequency());
            cfg.set("fillTask.x", fillTask.refX());
            cfg.set("fillTask.z", fillTask.refZ());
            cfg.set("fillTask.length", fillTask.refLength());
            cfg.set("fillTask.total", fillTask.refTotal());
            cfg.set("fillTask.forceLoad", fillTask.refForceLoad());
        }
        else
            cfg.set("fillTask", null);

        plugin.saveConfig();

        if (logIt)
            logConfig("Configuration saved.");
    }
}
