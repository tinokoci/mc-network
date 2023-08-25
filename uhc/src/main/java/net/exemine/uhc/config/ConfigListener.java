package net.exemine.uhc.config;

import lombok.Setter;
import net.exemine.uhc.UHC;
import org.bukkit.event.Listener;

public class ConfigListener implements Listener {

    @Setter
    protected UHC plugin;

    public void onEnable() {}

    public void onDisable() {}
}

