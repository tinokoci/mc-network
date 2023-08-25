package net.exemine.worldborder.events;

import net.exemine.worldborder.BorderData;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldFillerTaskCompleteEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private World world;
    private BorderData borderData;

    public WorldFillerTaskCompleteEvent(World world, BorderData borderData) {
        this.world = world;
        this.borderData = borderData;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public World getWorld() {
        return world;
    }

    public BorderData getBorderData() {
        return borderData;
    }
}
