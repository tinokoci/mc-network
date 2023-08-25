package net.exemine.uhc.scenario.impl;

import net.exemine.api.util.string.CC;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.UHC;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.event.GracePeriodEndEvent;
import net.exemine.uhc.scenario.ScenarioListener;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class RadarScenario extends ScenarioListener {

    private static final String RADAR_NAME = CC.PINK + "Radar " + CC.GRAY + "(Right Click)";

    private final GameService gameService = UHC.get().getGameService();
    private final UHCUserService userService = UHC.get().getUserService();

    @EventHandler
    public void onCompassClick(PlayerInteractEvent event) {
        if (!gameService.isPvP()) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (isRadarItem(event.getItem())) {
                UHCUser user = userService.get(event.getPlayer());
                if (!user.isPlaying()) return;

                Optional<UHCUser> closest = getClosestAlive(user);
                closest.ifPresentOrElse(
                        other -> user.sendMessage(CC.PURPLE + "[Radar] " + CC.GRAY + "You're tracking " +
                                other.getColoredDisplayName() + CC.GRAY + " in a distance of " +
                                CC.PINK + (int) user.getLocation().distance(other.getLocation()) + CC.GRAY + " blocks."),
                        () -> user.sendMessage(CC.RED + "No player found to track."));
            }
        }
    }

    @EventHandler
    public void onGracePeriodEnd(GracePeriodEndEvent event) {
        userService.getOnlineUsers().stream()
                .filter(UHCUser::isPlaying)
                .forEach(user -> {
                    // If their inventory is full, drop the item
                    if (!user.getInventory().addItem(getRadarItem()).isEmpty()) {
                        user.getWorld().dropItemNaturally(user.getLocation(), getRadarItem());
                    }
                });
    }

    private ItemStack getRadarItem() {
        return new ItemBuilder(Material.COMPASS)
                .setName(RADAR_NAME)
                .build();
    }

    private boolean isRadarItem(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(RADAR_NAME);
    }

    private Optional<UHCUser> getClosestAlive(UHCUser user) {
        Predicate<UHCUser> filter = other -> other.isPlaying() &&
                !other.getUniqueId().equals(user.getUniqueId()) &&
                (!gameService.isTeamGame() || !other.getTeam().equals(user.getTeam()) &&
                        !other.getTeam().isCrossTeamingWith(user.getTeam()));

        return user.getWorld().getPlayers()
                .stream()
                .map(other -> userService.get(other))
                .filter(filter)
                .min(Comparator.comparingDouble(other -> other.getLocation().distanceSquared(user.getLocation())));
    }
}
