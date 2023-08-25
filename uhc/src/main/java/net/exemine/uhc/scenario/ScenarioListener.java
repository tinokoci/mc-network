package net.exemine.uhc.scenario;

import lombok.Setter;
import net.exemine.uhc.UHC;
import org.bukkit.event.Listener;

public class ScenarioListener implements Listener {

    @Setter
    protected UHC plugin;

    protected void onEnable() {}

    protected void onDisable() {}
}
