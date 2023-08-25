package net.exemine.uhc.user;

import com.lunarclient.bukkitapi.object.StaffModule;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.Executor;
import net.exemine.api.util.callable.Callback;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.lunar.impl.LunarBorder;
import net.exemine.core.lunar.impl.LunarStaffModule;
import net.exemine.core.lunar.impl.LunarWaypoint;
import net.exemine.core.util.MessageUtil;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import net.exemine.core.util.spigot.Clickable;
import net.exemine.uhc.UHC;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.location.LocationService;
import net.exemine.uhc.user.event.UHCUserChangeStateEvent;
import net.exemine.uhc.user.info.GameInfo;
import net.exemine.uhc.user.info.RespawnInfo;
import net.exemine.uhc.world.WorldService;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UserListener implements Listener {

    private final UHC plugin;
    private final GameService gameService;
    private final LocationService locationService;
    private final UHCUserService userService;
    private final WorldService worldService;

    public UserListener(UHC plugin) {
        this.plugin = plugin;
        this.gameService = plugin.getGameService();
        this.locationService = plugin.getLocationService();
        this.userService = plugin.getUserService();
        this.worldService = plugin.getWorldService();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        if (plugin.isShuttingDown()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "This UHC server is shutting down.");
            return;
        }
        if (gameService.isState(GameState.SCATTERING)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "This UHC server is currently in the scatter phase, please wait for it to finish before joining back.");
            return;
        }
        UHCUser user = userService.retrieve(event.getUniqueId());

        if (gameService.isState(GameState.ENDING) && !user.isEqualOrAbove(RankType.STAFF)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "You cannot join this UHC server because it's currently in the end phase.");
            return;
        }
        if (user.isPlaying() || user.getGameInfo().isKilledByBannedUser() || user.isEqualOrAbove(RankType.STAFF)) {
            event.allow();
            return;
        }
        if (ToggleOption.WHITELIST.isEnabled() && !gameService.isWhitelisted(user) && !gameService.isStateOrHigher(GameState.PLAYING)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "This UHC server is currently whitelisted.");
            return;
        }
        if (userService.getOnlineUsers()
                .stream()
                .filter(onlineUser -> !onlineUser.isGameModerator())
                .count() >= NumberOption.SLOTS.getValue() && !user.isEqualOrAbove(RankType.DONATOR)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "This UHC server is currently full. You can purchase a rank @ " + CC.BOLD + Lang.STORE + CC.RED + " to bypass this restriction.");
            return;
        }
        if (!user.canLateScatter() && ToggleOption.SPECTATING.isDisabled() && gameService.isStateOrHigher(GameState.PLAYING)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "This UHC server has spectating disabled and your late scatter time has expired.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        UHCUser user = userService.get(event.getPlayer());
        GameState gameState = gameService.getState();
        List<Callback> messages = new ArrayList<>();

        if (!user.isPlaying()) { // don't mess with the player if he's playing the game
            messages.add(() -> user.sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------"));
            messages.add(user::sendMessage);
            messages.add(() -> user.sendMessage(CC.GRAY + " Welcome to match " + CC.PINK + '#' + gameService.getMatchNumber() + CC.GRAY + " of the " + CC.BOLD_PURPLE + Lang.SERVER_NAME + " UHC" + CC.GRAY + " season one!"));
            messages.add(user::sendMessage);
            messages.add(() -> new Clickable()
                    .add(Lang.LIST_PREFIX + CC.PINK + "Configuration: ")
                    .add(CC.BOLD_GOLD + "CLICK HERE", CC.GREEN + "Click to see game information.", "/config")
                    .add(CC.WHITE + " (/config)")
                    .send(user));
            messages.add(() -> new Clickable()
                    .add(Lang.LIST_PREFIX + CC.PINK + "Scenarios: ")
                    .add(CC.BOLD_GOLD + "CLICK HERE", CC.GREEN + "Click to see active scenarios.", "/scenarios")
                    .add(CC.WHITE + " (/scenarios)")
                    .send(user));
            messages.add(user::sendMessage);
        }
        switch (gameState) {
            case WORLD_GENERATION:
            case LOBBY:
                if (user.isSpectating()) {
                    user.setState(user.getState()); // if spectating, just refresh state
                } else {
                    user.setState(UHCUserState.LOBBY); // else set to lobby state
                }
                messages.add(() -> user.sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------"));
                break;
            case SCATTERING: // shouldn't happen, but just in case
                user.kickPlayer(CC.RED + "This UHC server is currently in the scatter phase, please wait for it to finish before joining back.");
                break;
            case PLAYING:
            case ENDING:
                GameInfo gameInfo = user.getGameInfo();
                if (gameInfo.isKilledByBannedUser()) {
                    user.setState(UHCUserState.IN_GAME);
                    MessageUtil.send(user.getColoredDisplayName() + CC.YELLOW + '[' + gameInfo.getKills().getValue() + ']' + CC.GRAY + " got respawned because they were killed by a person that was punished.");
                    Executor.schedule(() -> user.sendMessage(CC.GREEN + "You've been respawned because the person that killed you got banned.")).runSyncLater(300L);
                } else if (user.isWaiting()) { // if user joined for the first time, set him to spectator
                    user.setState(UHCUserState.SPECTATOR);
                } else if (user.isSpectating()) { // if user is already spectating, refresh his state (regular spec or mod)
                    user.setState(user.getState());
                } else if (user.isScattering()) { // if stuck in scatter, refresh state to setup him and immediately put him into playing state
                    user.setState(UHCUserState.SCATTER);
                    user.setState(UHCUserState.IN_GAME);
                }
                if (!user.isPlaying()) {
                    if (user.isGameModerator()) {
                        messages.add(() -> user.sendMessage(CC.ITALIC_GRAY + " (You're currently moderating this game.)"));
                    } else {
                        messages.add(() -> {
                            Clickable clickable = new Clickable(CC.ITALIC_GRAY + " (The game is in progress.) ");
                            if (user.getGameInfo().isDied()) {
                                clickable.add(CC.BOLD_RED + "You've died.");
                            } else if (user.canLateScatter()) {
                                clickable.add(CC.BOLD_YELLOW + "SCATTER ME", CC.GREEN + "Click here to late scatter.", "/latescatter");
                            } else {
                                clickable.add(CC.BOLD_RED + "Scatter expired.");
                            }
                            clickable.send(user);
                        });
                    }
                    messages.add(user::sendMessage);
                    messages.add(() -> user.sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------"));
                }
                // When user with PLAYING state joins, it doesn't reset their state which
                // handles visibility therefore we have to do it here
                if (user.isPlaying()) {
                    user.applyRelogInfo();
                    userService.getOnlineUsers().forEach(online -> {
                        online.hidePlayer(user);
                        online.showPlayer(user); // show user to everyone

                        // Hide all spectators from the user
                        if (online.isSpectating()) {
                            user.hidePlayer(online);
                        }
                    });
                }
        }
        Executor.schedule(() -> {
            if (user.isOnline()) {
                messages.forEach(Callback::run);
            }
        }).runSyncLater(150L);

        if (worldService.isWorld(user.getWorld(), worldService.getUhcWorld(), worldService.getNetherWorld())) {
            new LunarBorder(user.getWorld(), (user.getWorld() == worldService.getUhcWorld()
                    ? worldService.getBorderService().getCurrentRadius().getValue()
                    : worldService.getBorderService().getNetherBorder())
            ).send(event.getPlayer());

            if (worldService.isWorld(user.getWorld(), worldService.getUhcWorld())) {
                new LunarWaypoint("Center", new Location(
                        worldService.getUhcWorld(),
                        0,
                        worldService.getUhcWorld().getHighestBlockYAt(0, 0) + 2, 0)
                ).send(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UHCUser user = userService.get(event.getPlayer());

        if (user.isPlaying()) {
            user.saveRelogInfo();
        }
    }

    @EventHandler
    public void onUHCUserChangeState(UHCUserChangeStateEvent event) {
        UHCUser user = event.getUser();
        UHCUserState oldState = event.getOldState();

        PlayerInventory inventory = user.getInventory();
        boolean diedWhilePlaying = oldState == UHCUserState.IN_GAME;
        boolean oldStateWasGameModerator = Stream.of(UHCUserState.HOST, UHCUserState.SUPERVISOR, UHCUserState.MODERATOR).anyMatch(state -> state == oldState);

        // Update anti xray bypass
        user.setBypassAntiXray(user.isGameModerator());
        new LunarStaffModule(
                user.isGameModerator(),
                StaffModule.XRAY,
                StaffModule.BUNNY_HOP,
                StaffModule.NAME_TAGS)
                .send(user);

        // This is an exception of manually updating nametags because we don't want
        // the update delay to expose how our internal systems look to regular players
        if (oldStateWasGameModerator) {
            user.instantNametagRefresh();
        }
        switch (user.getState()) {
            case LOBBY:
                user.clearInventoryAndArmor();
                user.spigot().setCollidesWithEntities(true);
                user.setHealth(20D);
                user.setFoodLevel(20);
                user.setExp(0);
                user.setLevel(0);
                user.setGameMode(GameMode.ADVENTURE);
                user.teleport(locationService.getLobbySpawnLocation());

                inventory.setItem(0, new ItemBuilder(Material.CAULDRON_ITEM).setName(CC.PINK + "Game Information" + CC.GRAY + " (Right Click)").build());
                inventory.setItem(1, new ItemBuilder(Material.RAW_FISH).setDurability(3).setName(CC.PINK + "Scenarios" + CC.GRAY + " (Right Click)").build());
                inventory.setItem(4, new ItemBuilder(Material.IRON_SWORD).setName(CC.PINK + "Join Practice" + CC.GRAY + " (Right Click)").build());
                inventory.setItem(7, new ItemBuilder(Material.FIREWORK).setName(CC.PINK + "Leaderboards" + CC.GRAY + " (Right Click)").build());
                inventory.setItem(8, new ItemBuilder(Material.DOUBLE_PLANT).setName(CC.PINK + "Your Stats" + CC.GRAY + " (Right Click)").build());

                // Practice has same visibility so no need to update
                if (oldState != UHCUserState.PRACTICE) {
                    userService.getOnlineUsers().forEach(online -> {
                        online.showPlayer(user); // show user to everyone

                        // Hide all spectators from user because this state change fires on join as well
                        if (online.isSpectating()) {
                            user.hidePlayer(online);
                        }
                    });
                }
                break;
            case PRACTICE:
                user.clearInventoryAndArmor();
                user.spigot().setCollidesWithEntities(true);
                user.setHealth(20D);
                user.setFoodLevel(20);
                user.setGameMode(GameMode.ADVENTURE);

                if (oldState != UHCUserState.PRACTICE) {
                    user.teleport(locationService.getPracticeScatterLocation());
                }
                inventory.setContents(ItemUtil.deserializeItemArray(user.getData().getPracticeLayout()));
                inventory.setHelmet(new ItemBuilder(Material.IRON_HELMET).unbreakable().build());
                inventory.setChestplate(new ItemBuilder(Material.DIAMOND_CHESTPLATE).unbreakable().build());
                inventory.setLeggings(new ItemBuilder(Material.DIAMOND_LEGGINGS).unbreakable().build());
                inventory.setBoots(new ItemBuilder(Material.IRON_BOOTS).unbreakable().build());
                inventory.setHeldItemSlot(0);
                user.updateInventory();
                // No need to update visibility because old state must be LOBBY meaning user is visible
                break;
            case SCATTER:
                if (user.isDead()) user.spigot().respawn();

                user.teleport(user.getTeam().getScatterLocation());
                user.clearInventoryAndArmor();
                user.setGameMode(GameMode.SURVIVAL);
                user.setFallDistance(0F);
                user.setHealth(20D);
                user.setFoodLevel(20);
                user.setHealthScale(20D);
                user.setFlying(false);
                user.setAllowFlight(false);
                user.setLevel(0);
                user.setExp(0);
                user.setExhaustion(0F);
                user.setSaturation(20F);
                user.setSitting(true);
                user.spigot().setCollidesWithEntities(true);
                user.getEntityPlayer().getDataWatcher().watch(9, (byte) 0);
                user.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, NumberOption.STARTER_FOOD.getValue()));

                userService.getOnlineUsers().forEach(online -> {
                    online.showPlayer(user); // show user to everyone

                    // Hide all spectators from user because this state change fires on join as well
                    if (online.isSpectating()) {
                        user.hidePlayer(online);
                    }
                });
                break;
            case IN_GAME:
                if (user.hasRespawnInfo()) {
                    RespawnInfo info = user.getRespawnInfo();
                    user.setGameMode(GameMode.SURVIVAL);
                    user.setFallDistance(0F);
                    user.setFlying(false);
                    user.setAllowFlight(false);
                    user.setHealth(20);
                    user.setFoodLevel(20);
                    user.setSaturation(20F);
                    user.setInvulnerableTicks(10 * 20);
                    inventory.setContents(info.getInventory());
                    inventory.setArmorContents(info.getArmor());
                    user.setExp(info.getExp());
                    user.setLevel(info.getLevel());
                    user.teleport(info.getRespawnLocation());
                    user.getAntiChugInfo().clear();
                    user.setRespawnInfo(null);
                }
                user.setSitting(false);
                user.spigot().setCollidesWithEntities(true);
                user.getEntityPlayer().getDataWatcher().watch(9, (byte) 0);
                user.getGameInfo().setPlayed(true);
                user.setInvulnerableTicks(10 * 20);

                userService.getOnlineUsers().forEach(online -> {
                    online.hidePlayer(user); // just in case something is bugged
                    online.showPlayer(user); // reshow user to everyone
                    // No need to hide spectators because old state is always SCATTER which does that
                });
                break;
            case SPECTATOR:
            case MODERATOR:
            case SUPERVISOR:
            case HOST:
                if (user.isOffline()) return;

                user.clearInventoryAndArmor();
                user.setExp(0);
                user.setLevel(0);
                user.setGameMode(GameMode.CREATIVE);
                user.spigot().setCollidesWithEntities(false);
                user.setAllowFlight(true);
                user.setFlying(true);

                // Give user spectator items
                if (user.isGameModerator()) {
                    inventory.setItem(0, new ItemBuilder().setMaterial(Material.COMPASS).setName(CC.PINK + "Player Tracker " + CC.GRAY + "(Right Click)").build());
                    inventory.setItem(1, new ItemBuilder().setMaterial(Material.DIAMOND_PICKAXE).setName(CC.PINK + "Players Underground " + CC.GRAY + "(Right Click)").build());
                    inventory.setItem(2, new ItemBuilder().setMaterial(Material.NETHER_BRICK_ITEM).setName(CC.PINK + "Players In Nether " + CC.GRAY + "(Right Click)").build());
                    inventory.setItem(4, new ItemBuilder().setMaterial(Material.SUGAR).setName(CC.PINK + "Random Teleport " + CC.GRAY + "(Right Click)").build());
                    inventory.setItem(6, new ItemBuilder()
                            .setMaterial(Material.CARPET)
                            .setDurability(DyeColor.PINK.getWoolData())
                            .setName(CC.PINK + "Hide Hands " + CC.GRAY + "(Hold)")
                            .build());
                    inventory.setItem(7, new ItemBuilder().setMaterial(Material.BOOK).setName(CC.PINK + "Inspect Inventory " + CC.GRAY + "(Right Click)").build());
                    inventory.setItem(8, new ItemBuilder().setMaterial(Material.WATCH).setName(CC.PINK + "Your Settings " + CC.GRAY + "(Right Click)").build());
                } else {
                    inventory.setItem(0, new ItemBuilder().setMaterial(Material.COMPASS).setName(CC.PINK + "Player Tracker " + CC.GRAY + "(Right Click)").build());
                    inventory.setItem(1, new ItemBuilder().setMaterial(Material.WATCH).setName(CC.PINK + "Your Settings " + CC.GRAY + "(Right Click)").build());

                    if (!user.getGameInfo().isDied() && user.canLateScatter()) {
                        inventory.setItem(4, new ItemBuilder().setMaterial(Material.PAPER).setName(CC.PINK + "Late Scatter " + CC.GRAY + "(Right Click)").build());
                    }
                    inventory.setItem(7, new ItemBuilder()
                            .setMaterial(Material.CARPET)
                            .setDurability(DyeColor.PINK.getWoolData())
                            .setName(CC.PINK + "Hide Hands " + CC.GRAY + "(Hold)")
                            .build());
                    inventory.setItem(8, new ItemBuilder().setMaterial(Material.REDSTONE).setName(CC.PINK + "Exit " + CC.GRAY + "(Right Click)").build());
                }

                // Teleport user to center
                user.setSpectatorTeleportDelay(true);
                Executor.schedule(() -> {
                    if (user.isPlaying()) return; // if player gets a respawn don't do this
                    user.teleport(diedWhilePlaying || gameService.isStateOrHigher(GameState.SCATTERING)
                            ? worldService.getUhcWorld().getSpawnLocation()
                            : locationService.getLobbySpawnLocation());
                    user.setAllowFlight(true);
                    user.setFlying(true);
                    user.setSpectatorTeleportDelay(false);
                }).runSyncLater(diedWhilePlaying ? 5000L : 0L); // give the player 5 seconds before teleporting him if he died

                // Setup visibility
                if (user.isRegularSpectator()) {
                    userService.getOnlineUsers().forEach(online -> {
                        if (!online.isSpectating()) {
                            user.showPlayer(online);
                            online.hidePlayer(user);
                            return;
                        }
                        // Handle Online Players
                        if (online.getData().isShowSpectators()) {
                            online.showPlayer(user);
                        } else {
                            online.hidePlayer(user);
                        }
                        // Handle User
                        if (online.isGameModerator()) {
                            user.hidePlayer(online);
                            return;
                        }
                        if (online.isRegularSpectator() && user.getData().isShowSpectators()) {
                            user.showPlayer(online);
                        } else {
                            user.hidePlayer(online);
                        }
                    });
                }
                if (user.isGameModerator()) {
                    userService.getOnlineUsers().forEach(online -> {
                        if (!online.isSpectating()) {
                            user.showPlayer(online);
                            online.hidePlayer(user);
                            return;
                        }
                        // Handle Online Players
                        if (online.isGameModerator() && online.getStaffData().isShowGameModerators()) {
                            online.showPlayer(user);
                        } else {
                            online.hidePlayer(user);
                        }
                        // Handle User
                        if ((online.isRegularSpectator() && user.getData().isShowSpectators())
                                || (online.isGameModerator() && user.getStaffData().isShowGameModerators())) {
                            user.showPlayer(online);
                        } else {
                            user.hidePlayer(online);
                        }
                    });
                }
                break;
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (worldService.isWorld(event.getPlayer().getWorld(), worldService.getUhcWorld(), worldService.getNetherWorld())) {
            new LunarBorder(event.getPlayer().getWorld(), (event.getPlayer().getWorld() == worldService.getUhcWorld() ?
                    worldService.getBorderService().getCurrentRadius().getValue() : worldService.getBorderService().getNetherBorder()))
                    .send(event.getPlayer());

            if (worldService.isWorld(event.getPlayer().getWorld(), worldService.getUhcWorld())) {
                new LunarWaypoint("Center", new Location(
                        worldService.getUhcWorld(),
                        0,
                        worldService.getUhcWorld().getHighestBlockYAt(0, 0) + 2, 0)
                ).send(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof PlayerInventory)) return;
        UHCUser user = userService.get(event.getWhoClicked().getUniqueId());

        if (!user.isInPractice() && !user.isScattering() && !user.isPlaying()) {
            event.setCancelled(true);
        }
    }

    /*@EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player &&
                ((Player) event.getEntity()).getNoDamageTicks() > 0) {
            event.setCancelled(true);
            System.out.println("Cancel because damage ticks were " + ((Player) event.getEntity()).getNoDamageTicks());
        }
    }*/
}