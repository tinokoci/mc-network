package net.exemine.worldborder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WBCommand implements CommandExecutor {

    private WorldBorder plugin;

    // color values for strings
    private final String clrCmd = ChatColor.AQUA.toString();		// main commands
    private final String clrReq = ChatColor.GREEN.toString();		// required values
    private final String clrOpt = ChatColor.DARK_GREEN.toString();	// optional values
    private final String clrDesc = ChatColor.WHITE.toString();		// command descriptions
    private final String clrHead = ChatColor.YELLOW.toString();		// command listing header
    private final String clrErr = ChatColor.RED.toString();			// errors / notices

    WBCommand(WorldBorder plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split)
    {
        Player player = (sender instanceof Player) ? (Player)sender : null;

        String cmd = clrCmd + ((player == null) ? "wb" : "/wb");
        String cmdW = clrCmd + ((player == null) ? "wb " + clrReq + "<world>" : "/wb " + clrOpt + "[world]") + clrCmd;

        // if world name is passed inside quotation marks, handle that
        if (split.length > 2 && split[0].startsWith("\""))
        {
            if (split[0].endsWith("\""))
            {
                split[0] = split[0].substring(1, split[0].length() - 1);
            }
            else
            {
                List<String> args = new ArrayList<String>();
                StringBuilder quote = new StringBuilder(split[0]);
                int loop;
                for (loop = 1; loop < split.length; loop++)
                {
                    quote.append(" ").append(split[loop]);
                    if (split[loop].endsWith("\""))
                        break;
                }

                if (loop < split.length || !split[loop].endsWith("\""))
                {
                    args.add(quote.substring(1, quote.length() - 1));
                    loop++;
                    while (loop < split.length)
                    {
                        args.add(split[loop]);
                        loop++;
                    }
                    split = args.toArray(new String[0]);
                }
            }
        }

        // "set" command from player or console, world specified
        if ((split.length >= 4) && split[1].equalsIgnoreCase("set"))
        {
            if (Config.HasPermission(player, "set")) return true;

            if (split.length == 4 && ! split[split.length - 1].equalsIgnoreCase("spawn"))
            {	// command can only be this short if "spawn" is specified rather than x + z or player name
                sender.sendMessage(clrErr + "You have not provided a sufficient number of arguments. Check command list using root /wb command.");
                return true;
            }

            World world = sender.getServer().getWorld(split[0]);
            if (world == null)
                sender.sendMessage("The world you specified (\"" + split[0] + "\") could not be found on the server, but data for it will be stored anyway.");

            if (cmdSet(sender, world, player, split, 2))
                sender.sendMessage("Border has been set. " + Config.BorderDescription(split[0]));
        }

        // "set" command from player using current world since it isn't specified, or allowed from console only if player name is specified
        else if ((split.length >= 2) && split[0].equalsIgnoreCase("set"))
        {
            if (Config.HasPermission(player, "set")) return true;

            if (player == null)
            {
                if (! split[split.length - 2].equalsIgnoreCase("player"))
                {	// command can only be called by console without world specified if player is specified instead
                    sender.sendMessage(clrErr + "You must specify a world name from console if not specifying a player name. Check command list using root \"wb\" command.");
                    return true;
                }
                player = Bukkit.getPlayer(split[split.length - 1]);
                if (player == null || ! player.isOnline())
                {
                    sender.sendMessage(clrErr + "The player you specified (\"" + split[split.length - 1] + "\") does not appear to be online.");
                    return true;
                }
            }

            if (cmdSet(sender, player.getWorld(), player, split, 1))
                sender.sendMessage("Border has been set. " + Config.BorderDescription(player.getWorld().getName()));
        }

        // "shape" command from player or console
        else if (split.length == 2 && split[0].equalsIgnoreCase("shape"))
        {
            if (Config.HasPermission(player, "shape")) return true;

            if (split[1].equalsIgnoreCase("rectangular") || split[1].equalsIgnoreCase("square"))
                Config.setShape(false);
            else if (split[1].equalsIgnoreCase("elliptic") || split[1].equalsIgnoreCase("round"))
                Config.setShape(true);
            else
            {
                sender.sendMessage("You must specify a shape of \"elliptic\"/\"round\" or \"rectangular\"/\"square\".");
                return true;
            }

            if (player != null)
                sender.sendMessage("Default border shape for all worlds is now set to \"" + Config.ShapeName() + "\".");
        }

        // "debug" command from player or console
        else if (split.length == 2 && split[0].equalsIgnoreCase("debug"))
        {
            if (Config.HasPermission(player, "debug")) return true;

            Config.setDebug(strAsBool(split[1]));

            if (player != null)
                Config.log((Config.Debug() ? "Enabling" : "Disabling") + " debug output at the command of player \"" + player.getName() + "\".");

            if (player != null)
                sender.sendMessage("Debug mode " + enabledColored(Config.Debug()) + ".");
        }

        // "fill" command from player or console, world specified
        else if (split.length >= 2 && split[1].equalsIgnoreCase("fill"))
        {
            // GNote - Fix this...console should be able to do this too
            if (player != null && !player.hasPermission("crate.uhc.mod")) return true;

            boolean cancel = false, confirm = false, pause = false;
            String frequency = "";
            if (split.length >= 3)
            {
                cancel = split[2].equalsIgnoreCase("cancel") || split[2].equalsIgnoreCase("stop");
                confirm = split[2].equalsIgnoreCase("confirm");
                pause = split[2].equalsIgnoreCase("pause");
                if (!cancel && !confirm && !pause)
                    frequency = split[2];
            }
            String pad = (split.length >= 4) ? split[3] : "";
            String forceLoad = (split.length >= 5) ? split[4] : "";

            String world = split[0];

            cmdFill(sender, player, world, confirm, cancel, pause, pad, frequency, forceLoad);
        }

        // "fill" command from player (or from console solely if using cancel or confirm), using current world
        else if (split.length >= 1 && split[0].equalsIgnoreCase("fill"))
        {
            if (Config.HasPermission(player, "fill")) return true;

            boolean cancel = false, confirm = false, pause = false;
            String frequency = "";
            if (split.length >= 2)
            {
                cancel = split[1].equalsIgnoreCase("cancel") || split[1].equalsIgnoreCase("stop");
                confirm = split[1].equalsIgnoreCase("confirm");
                pause = split[1].equalsIgnoreCase("pause");
                if (!cancel && !confirm && !pause)
                    frequency = split[1];
            }
            String pad = (split.length >= 3) ? split[2] : "";
            String forceLoad = (split.length >= 4) ? split[3] : "";

            String world = "";
            if (player != null && !cancel && !confirm && !pause)
                world = player.getWorld().getName();

            if (!cancel && !confirm && !pause && world.isEmpty())
            {
                sender.sendMessage("You must specify a world! Example: " + cmdW + " fill " + clrOpt + "[freq] [pad] [force]");
                return true;
            }

            cmdFill(sender, player, world, confirm, cancel, pause, pad, frequency, forceLoad);
        }

        // we couldn't decipher any known commands, so show help
        else {
            if (Config.HasPermission(player, "help")) return true;

            sender.sendMessage(cmd+" set " + clrReq + "<radiusX> " + clrOpt + "[radiusZ]" + clrDesc + " - set border, centered on you.");
            sender.sendMessage(cmdW+" set " + clrReq + "<radiusX> " + clrOpt + "[radiusZ] <x> <z>" + clrDesc + " - set border.");
            sender.sendMessage(cmdW+" set " + clrReq + "<radiusX> " + clrOpt + "[radiusZ] spawn" + clrDesc + " - use spawn point.");
            sender.sendMessage(cmd+" set " + clrReq + "<radiusX> " + clrOpt + "[radiusZ] player <name>" + clrDesc + " - center on player.");
            sender.sendMessage(cmd+" shape " + clrReq + "<elliptic|rectangular>" + clrDesc + " - set the default shape.");
            sender.sendMessage(cmd+" shape " + clrReq + "<round|square>" + clrDesc + " - same as above.");
            sender.sendMessage(cmdW+" fill " + clrOpt + "[freq] [pad] [force]" + clrDesc + " - fill world to border.");
        }

        return true;
    }


    private boolean strAsBool(String str)
    {
        str = str.toLowerCase();
        return str.startsWith("y") || str.startsWith("t") || str.startsWith("on") || str.startsWith("+") || str.startsWith("1");
    }

    private String enabledColored(boolean enabled)
    {
        return enabled ? clrReq+"enabled" : clrErr+"disabled";
    }

    private boolean cmdSet(CommandSender sender, World world, Player player, String[] data, int offset)
    {
        int radiusX, radiusZ;
        double x, z;
        int radiusCount = data.length - offset;

        try
        {
            if (data[data.length - 1].equalsIgnoreCase("spawn"))
            {	// "spawn" specified for x/z coordinates
                Location loc = world.getSpawnLocation();
                x = loc.getX();
                z = loc.getZ();
                radiusCount -= 1;
            }
            else if (data[data.length - 2].equalsIgnoreCase("player"))
            {	// player name specified for x/z coordinates
                Player playerT = Bukkit.getPlayer(data[data.length - 1]);
                if (playerT == null || ! playerT.isOnline())
                {
                    sender.sendMessage(clrErr + "The player you specified (\"" + data[data.length - 1] + "\") does not appear to be online.");
                    return false;
                }
                world = playerT.getWorld();
                x = playerT.getLocation().getX();
                z = playerT.getLocation().getZ();
                radiusCount -= 2;
            }
            else
            {
                if (player == null || radiusCount > 2)
                {	// x and z specified
                    x = Double.parseDouble(data[data.length - 2]);
                    z = Double.parseDouble(data[data.length - 1]);
                    radiusCount -= 2;
                }
                else
                {	// using coordinates of command sender (player)
                    x = player.getLocation().getX();
                    z = player.getLocation().getZ();
                }
            }

            radiusX = Integer.parseInt(data[offset]);
            if (radiusCount < 2)
                radiusZ = radiusX;
            else
                radiusZ = Integer.parseInt(data[offset+1]);
        }
        catch(NumberFormatException ex)
        {
            sender.sendMessage(clrErr + "The radius value(s) must be integers and the x and z values must be numerical.");
            return false;
        }

        Config.setBorder(world.getName(), radiusX, radiusZ, x, z);
        return true;
    }


    private String fillWorld = "";
    private int fillFrequency = 20;
    private int fillPadding = CoordXZ.chunkToBlock(13);
    private boolean fillForceLoad = false;

    private void fillDefaults()
    {
        fillWorld = "";
        fillFrequency = 20;
        // with "view-distance=10" in server.properties and "Render Distance: Far" in client, hitting border during testing
        // was loading 11 chunks beyond the border in a couple of directions (10 chunks in the other two directions); thus:
        fillPadding = CoordXZ.chunkToBlock(13);
        fillForceLoad = false;
    }

    private void cmdFill(CommandSender sender, Player player, String world, boolean confirm, boolean cancel, boolean pause, String pad, String frequency, String forceLoad)
    {
        if (cancel)
        {
            sender.sendMessage(clrHead + "Cancelling the world map generation task.");
            fillDefaults();
            Config.StopFillTask();
            return;
        }

        if (pause)
        {
            if (Config.fillTask == null || !Config.fillTask.valid())
            {
                sender.sendMessage(clrHead + "The world map generation task is not currently running.");
                return;
            }
            Config.fillTask.pause();
            sender.sendMessage(clrHead + "The world map generation task is now " + (Config.fillTask.isPaused() ? "" : "un") + "paused.");
            return;
        }

        if (Config.fillTask != null && Config.fillTask.valid())
        {
            sender.sendMessage(clrHead + "The world map generation task is already running.");
            return;
        }

        // set padding and/or delay if those were specified
        try
        {
            if (!pad.isEmpty())
                fillPadding = Math.abs(Integer.parseInt(pad));
            if (!frequency.isEmpty())
                fillFrequency = Math.abs(Integer.parseInt(frequency));
        }
        catch(NumberFormatException ex)
        {
            sender.sendMessage(clrErr + "The frequency and padding values must be integers.");
            fillDefaults();
            return;
        }
        if (fillFrequency <= 0)
        {
            sender.sendMessage(clrErr + "The frequency value must be greater than zero.");
            fillDefaults();
            return;
        }

        if (!forceLoad.isEmpty())
            fillForceLoad = strAsBool(forceLoad);

        // set world if it was specified
        if (!world.isEmpty())
            fillWorld = world;

        if (confirm)
        {	// command confirmed, go ahead with it
            if (fillWorld.isEmpty())
            {
                sender.sendMessage(clrErr + "You must first use this command successfully without confirming.");
                return;
            }

            if (player != null)
                Config.log("Filling out world to border at the command of player \"" + player.getName() + "\".");

            int ticks = 1, repeats = 1;
            if (fillFrequency > 20)
                repeats = fillFrequency / 20;
            else
                ticks = 20 / fillFrequency;

            Config.fillTask = new WorldFillTask(plugin.getServer(), player, fillWorld, fillPadding, repeats, ticks, fillForceLoad);
            if (Config.fillTask.valid())
            {
                int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, Config.fillTask, ticks, ticks);
                Config.fillTask.setTaskID(task);
                sender.sendMessage("WorldBorder map generation for world \"" + fillWorld + "\" task started.");
            }
            else
                sender.sendMessage(clrErr + "The world map generation task failed to start.");

            fillDefaults();
        }
        else
        {
            if (fillWorld.isEmpty())
            {
                sender.sendMessage(clrErr + "You must first specify a valid world.");
            }

        }
    }


    private String trimWorld = "";
    private int trimFrequency = 5000;
    private int trimPadding = CoordXZ.chunkToBlock(13);

}
