package net.exemine.worldborder.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldFillerTaskProgressEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private String worldName;
    private double percent;

    public WorldFillerTaskProgressEvent(String worldName, double percent) {
        this.worldName = worldName;
        this.percent = percent;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getPercent() {
        return percent;
    }
}
