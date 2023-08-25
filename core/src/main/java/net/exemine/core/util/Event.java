package net.exemine.core.util;

import org.bukkit.event.HandlerList;

public class Event extends org.bukkit.event.Event {

    public static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}