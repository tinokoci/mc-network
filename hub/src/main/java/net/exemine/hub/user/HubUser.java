package net.exemine.hub.user;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.impl.HubData;
import net.exemine.api.proxy.ProxyService;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.util.ServerUtil;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.hub.Hub;
import net.exemine.hub.location.LocationService;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.PlayerInventory;

import java.time.LocalDateTime;

@Getter
@Setter
public class HubUser extends ExeUser<HubData> {

    private final Hub plugin;
    private final LocationService locationService;
    private final ProxyService proxyService;

    private boolean playedBefore;

    public HubUser(Class<HubData> dataTypeClass, Hub plugin) {
        super(dataTypeClass, plugin.getUserService(), plugin.getCore());
        this.plugin = plugin;
        this.locationService = plugin.getLocationService();
        this.proxyService = plugin.getCore().getProxyService();
    }

    @Override
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        super.onConnect(event);
        playedBefore = getData().getFirstSeen() != null;

        if (!playedBefore) {
            getData().setFirstSeen(LocalDateTime.now());

        }
    }

    public void onJoin() {
        setup();

        if (!playedBefore) {
            Executor.schedule(() -> ServerUtil.launchFirework(getLocation(), Color.PURPLE, 2, true, true)).runSyncLater(1000L);
        }
        Executor.schedule(() -> {
            if (getCoreUser().isPunishedAllowedConnectForLink()) {
                playSound(Sound.ANVIL_BREAK);
                sendLinkRequiredMessage();
                setSitting(true);
                return;
            }
            playSound(Sound.LEVEL_UP);
            sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------");
            sendMessage();
            sendMessage(CC.GRAY + " Welcome " + (playedBefore ? "back " : "") + "to the " + CC.BOLD_PURPLE + Lang.SERVER_NAME + " Network" + CC.GRAY + '!');
            sendMessage();
            sendMessage(Lang.LIST_PREFIX + CC.PINK + "Website: " + CC.WHITE + Lang.WEBSITE);
            sendMessage(Lang.LIST_PREFIX + CC.PINK + "Discord: " + CC.WHITE + Lang.DISCORD);
            sendMessage(Lang.LIST_PREFIX + CC.PINK + "Store: " + CC.WHITE + Lang.STORE);
            sendMessage(Lang.LIST_PREFIX + CC.PINK + "Twitter: " + CC.WHITE + Lang.TWITTER);
            sendMessage();
            sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------");

            if (getData().isEdit()) {
                sendMessage(CC.PURPLE + "[Build] " + CC.GRAY + "You have build mode active.");
            }
        }).runSyncLater(150L);
    }

    private void setup() {
        if (isOffline()) return;

        setLevel(getCoreData().getLevel());
        setHealth(20);
        setFoodLevel(20);
        setSaturation(20);
        getActivePotionEffects().forEach(effect -> removePotionEffect(effect.getType()));
        setWalkSpeed(0.3F);
        teleportToSpawn();
        setBuild(getData().isEdit());
        setFlight(getData().isFlight());

        // Hide online players if user's player visibility is disabled and players aren't staff
        if (!getData().isPlayerVisibility()) {
            getUserService().getOnlineUsers()
                    .stream()
                    .filter(online -> !online.isEqualOrAbove(RankType.STAFF))
                    .forEach(this::hidePlayer);
        }

        // If player isn't staff, hide them for all online players who have player visibility disabled
        if (!isEqualOrAbove(RankType.STAFF)) {
            getUserService().getOnlineUsers()
                    .stream()
                    .filter(online -> !online.getData().isPlayerVisibility())
                    .forEach(online -> online.hidePlayer(this));
        }
    }

    private void setupHotbar() {
        if (isOffline()) return;

        PlayerInventory inventory = getInventory();

        inventory.clear();
        inventory.setArmorContents(null);
        inventory.setHeldItemSlot(0);

        inventory.setItem(0, new ItemBuilder(Material.COMPASS)
                .setName(CC.PINK + "Game Selector" + CC.GRAY + " (Right Click)")
                .build());

        inventory.setItem(4, new ItemBuilder(Material.CHEST)
                .setName(CC.PINK + "Your Settings" + CC.GRAY + " (Right Click)")
                .build());

        inventory.setItem(8, new ItemBuilder(Material.WATCH)
                .setName(CC.PINK + "Hub Selector" + CC.GRAY + " (Right Click)")
                .build());

        setupVisibilityItem(false);
        updateInventory();
    }

    public void setupVisibilityItem(boolean updateInventory) {
        getInventory().setItem(7, new ItemBuilder(Material.INK_SACK)
                .setDurability(getData().isPlayerVisibility() ? 10 : 8)
                .setName(CC.PINK + (getData().isPlayerVisibility() ? "Hide" : "Show") + " Players" + CC.GRAY + " (Right Click)")
                .build());
        if (updateInventory) {
            updateInventory();
        }
    }

    public void teleportToSpawn() {
        if (isOffline()) return;

        Location spawnLocation = locationService.getSpawnLocation();
        if (spawnLocation == null) return;

        if (getData().isFlight()) {
            spawnLocation = spawnLocation.clone().add(0, 0.1, 0);
        }
        teleport(spawnLocation);
    }

    public void setFlight(boolean flight) {
        getData().setFlight(flight);
        getPlayer().setAllowFlight(flight);

        if (!flight && isOnline()) {
            getPlayer().setFlying(false);
        }
    }

    public void setBuild(boolean build) {
        getData().setEdit(build);
        getPlayer().setGameMode(build ? GameMode.CREATIVE : GameMode.ADVENTURE);

        if (build) {
            getInventory().clear();
        } else {
            setupHotbar();
        }
    }

    public void sendLinkRequiredMessage() {
        sendMessage();
        sendMessage(CC.RED + "You're currently suspended from " + CC.BOLD + Lang.SERVER_NAME + " Network" + CC.RED + '!');
        sendMessage(CC.RED + "You've been allowed to join for you to synchronize your minecraft account to a discord account.");
        sendMessage();
    }
}
