package net.exemine.hub.user;

import com.execets.spigot.ExeSpigot;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.Executor;
import net.exemine.core.nms.npc.NPC;
import net.exemine.core.nms.npc.NPCInteractEvent;
import net.exemine.hub.Hub;
import net.exemine.hub.menu.GameSelectorMenu;
import net.exemine.hub.menu.HubSelectorMenu;
import net.exemine.hub.nms.NMSService;
import net.exemine.hub.user.event.PlayerToggleHubVisibilityEvent;
import net.exemine.hub.user.movement.HubMovementListener;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.stream.Stream;

public class UserListener implements Listener {

    private final HubUserService userService;
    private final InstanceService instanceService;
    private final NMSService nmsService;

    public UserListener(Hub plugin) {
        this.userService = plugin.getUserService();
        this.instanceService = plugin.getCore().getInstanceService();
        this.nmsService = plugin.getNmsService();
        ExeSpigot.INSTANCE.addMovementHandler(new HubMovementListener(plugin.getConfigFile(), plugin.getUserService()));
    }

    private boolean cannotBuild(Player player) {
        HubUser user = userService.get(player);
        return !user.getData().isEdit() || !user.isEqualOrAbove(Rank.DEVELOPER);
    }

    @EventHandler
    public void onPlayerToggleVisibility(PlayerToggleHubVisibilityEvent event) {
        HubUser user = event.getUser();
        user.setupVisibilityItem(true);

        userService.getOnlineUsers().forEach(online -> {
            // If user visibility is disabled and online user isn't staff
            if (!event.isEnabled() && !online.isEqualOrAbove(RankType.STAFF)) {
                user.hidePlayer(online);
            } else {
                user.showPlayer(online);
            }
        });
    }

    @EventHandler
    public void onNPCInteract(NPCInteractEvent event) {
        HubUser user = userService.get(event.getPlayer());
        if (user.getCoreUser().isPunishedAllowedConnectForLink()) return;

        if (user.getItemInHand().getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        NPC npc = event.getNpc();

        if (npc.equals(nmsService.getUhcNpc())) {
            instanceService.getAllInstances(InstanceType.UHC)
                    .stream()
                    .findAny()
                    .ifPresent(uhc -> Executor.schedule(() -> user.performCommand("join " + uhc.getName())).runSync());
        } else if (npc.equals(nmsService.getFfaNpc())) {
            instanceService.getAllInstances(InstanceType.FFA)
                    .stream()
                    .findAny()
                    .ifPresent(uhc -> Executor.schedule(() -> user.performCommand("join " + uhc.getName())).runSync());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        HubUser user = userService.get(event.getPlayer());
        Action action = event.getAction();
        ItemStack item = event.getItem();

        if (event.hasBlock() && cannotBuild(event.getPlayer())) event.setCancelled(true);
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (item == null || !item.hasItemMeta()) return;
        if (user.getCoreUser().isPunishedAllowedConnectForLink()) return;

        switch (item.getType()) {
            case COMPASS:
                new GameSelectorMenu(user).open();
                break;
            case CHEST:
                user.performCommand("settings");
                break;
            case INK_SACK:
                user.performCommand("visibility");
                break;
            case WATCH:
                new HubSelectorMenu(user).open();
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        HubUser user = userService.get(event.getPlayer());
        if (user.getCoreUser().isPunishedAllowedConnectForLink()
                && Stream.of("/link", "/sync").noneMatch(command -> event.getMessage().startsWith(command))) {
            event.setCancelled(true);
            user.sendLinkRequiredMessage();
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        HubUser user = userService.get(event.getPlayer());
        if (user.getGameMode() == GameMode.CREATIVE || user.getData().isFlight()) return;

        event.setCancelled(true);
        user.setAllowFlight(false);
        user.setFlying(false);
        user.setVelocity(user.getLocation().getDirection().multiply(1.5).setY(1));
        user.playSound(Sound.GHAST_FIREBALL);
        user.playEffect(user.getLocation(), Effect.MOBSPAWNER_FLAMES, 100);
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (cannotBuild(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (cannotBuild(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (cannotBuild(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (cannotBuild(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player) || cannotBuild((Player) event.getRemover()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        if (cannotBuild(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (cannotBuild((Player) event.getWhoClicked())
                && event.getClickedInventory() instanceof PlayerInventory)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (event.getBlock().getType() != Material.ICE
                || event.getBlock().getType() != Material.SNOW
                || event.getBlock().getType() != Material.SNOW_BLOCK) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // If creature is not spawned by the plugin
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }
}
