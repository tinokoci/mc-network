package net.exemine.uhc.spectator;

import com.execets.spigot.ExeSpigot;
import net.exemine.api.util.Cooldown;
import net.exemine.api.util.string.CC;
import net.exemine.core.util.LocationUtil;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.spectator.menu.ChestViewerMenu;
import net.exemine.uhc.spectator.menu.InventoryViewerMenu;
import net.exemine.uhc.spectator.menu.PlayerTrackerMenu;
import net.exemine.uhc.spectator.tracker.PlayerTrackerType;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftMinecartChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SpectatorListener implements Listener {

    private final GameService gameService;
    private final UHCUserService userService;
    private final Cooldown<UUID> randomTeleportCooldown = new Cooldown<>();

    public SpectatorListener(GameService gameService, UHCUserService userService) {
        this.gameService = gameService;
        this.userService = userService;
        ExeSpigot.INSTANCE.addMovementHandler(new SpectatorMoveInterceptor(userService));
    }

    private boolean isSpectator(Player player) {
        return userService.get(player).isSpectating();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        UHCUser user = userService.get(event.getPlayer());
        if (!user.isSpectating()) return;
        event.setCancelled(true);

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack itemInHand = user.getItemInHand();
        if (itemInHand == null) return;

        switch (itemInHand.getType()) {
            case COMPASS:
                PlayerTrackerType type = gameService.isStateOrLower(GameState.LOBBY) ? PlayerTrackerType.PRACTICE : PlayerTrackerType.ALL;
                new PlayerTrackerMenu(user, gameService, userService, type).open();
                break;
            case DIAMOND_PICKAXE:
                new PlayerTrackerMenu(user, gameService, userService, PlayerTrackerType.MINING).open();
                break;
            case NETHER_BRICK_ITEM:
                new PlayerTrackerMenu(user, gameService, userService, PlayerTrackerType.NETHER).open();
                break;
            case SUGAR:
                if (randomTeleportCooldown.isActive(user.getUniqueId())) return;

                List<UHCUser> users = (gameService.isStateOrHigher(GameState.SCATTERING) ? userService.getInGameUsers() : userService.getPracticeUsers())
                        .stream()
                        .filter(inGameUser -> user.isGameModerator() || inGameUser.isInNether() || LocationUtil.isInsideRadius(inGameUser, 100))
                        .collect(Collectors.toList());

                if (users.isEmpty()) {
                    user.sendMessage(CC.RED + "Cannot find a player to teleport you to.");
                    return;
                }
                UHCUser target = users.get(ThreadLocalRandom.current().nextInt(users.size()));

                randomTeleportCooldown.put(user.getUniqueId(), 1);
                user.teleport(target);
                user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You've teleported to " + target.getColoredDisplayName() + CC.GRAY + '.');
                break;
            case BOOK:
                if (action != Action.RIGHT_CLICK_BLOCK) return;

                Block block = event.getClickedBlock();
                if (block.getType() != Material.CHEST) return;

                Chest chest = (Chest) event.getClickedBlock().getState();
                Inventory inventory = chest.getInventory();

                new ChestViewerMenu(user, inventory.getSize(), inventory.getContents()).open();
                user.sendMessage(CC.GRAY + "You've silently opened the chest.");
                break;
            case WATCH:
                user.performCommand("settings");
                break;
            case PAPER:
                user.performCommand("latescatter");

                if (!user.canLateScatter()) {
                    itemInHand.setType(null);
                    user.updateInventory();
                }
                break;
            case REDSTONE:
                user.performCommand("hub");
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        UHCUser user = userService.get(event.getPlayer());

        if (!isSpectator(user) || user.getItemInHand().getType() != Material.BOOK) return;
        event.setCancelled(true);

        Entity clickedEntity = event.getRightClicked();

        if (clickedEntity instanceof Player) {
            UHCUser clickedUser = userService.get(event.getRightClicked().getUniqueId());
            new InventoryViewerMenu(user, clickedUser).open();
        }
        if (clickedEntity instanceof CraftMinecartChest) {
            CraftMinecartChest minecart = (CraftMinecartChest) event.getRightClicked();
            Inventory inventory = minecart.getInventory();

            new ChestViewerMenu(user, inventory.getSize(), inventory.getContents()).open();
        }
    }

    @EventHandler
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        if (isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && isSpectator((Player) event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player && isSpectator((Player) event.getAttacker())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player && isSpectator((Player) event.getTarget())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
        if (event.getEntity() instanceof Player && isSpectator((Player) event.getEntity())) {
            event.setCollisionCancelled(true);
        }
    }
}
