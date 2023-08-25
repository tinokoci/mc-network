package net.exemine.core.report;

import org.bukkit.entity.Player;

public interface ReportProvider {

    void teleport(Player player, Player target);
}