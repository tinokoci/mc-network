package net.exemine.uhc.game;

import net.exemine.api.match.MatchService;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.util.Executor;
import net.exemine.api.util.MathUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.elo.EloCalculator;
import net.exemine.core.util.LocationUtil;
import net.exemine.core.util.item.ItemUtil;
import net.exemine.core.util.spigot.Clickable;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.BorderRadius;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.game.event.GameEndEvent;
import net.exemine.uhc.game.event.GameStartEvent;
import net.exemine.uhc.logger.CombatLoggerEntity;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.team.TeamService;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;
import net.exemine.uhc.user.info.AntiChugInfo;
import net.exemine.uhc.user.info.GameInfo;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GameListener implements Listener {

    private final BorderService borderService;
    private final GameService gameService;
    private final UHCUserService userService;
    private final TeamService teamService;
    private final WorldService worldService;
    private final MatchService matchService;

    public GameListener(UHC plugin) {
        this.borderService = plugin.getBorderService();
        this.gameService = plugin.getGameService();
        this.userService = plugin.getUserService();
        this.teamService = plugin.getTeamService();
        this.worldService = plugin.getWorldService();
        this.matchService = plugin.getMatchService();
    }


    @EventHandler(priority = EventPriority.LOWEST) // run before scenarios (eg. SafeLoot etc.)
    public void onDeathAddDrops(PlayerDeathEvent event) {
        UHCUser user = userService.retrieve(event.getEntity()); // using retrieve because of the combat logger

        if (user.isPlaying()) { // user is still in playing state here, so we can use this check
            List<ItemStack> drops = event.getDrops();
            drops.addAll(user.getAntiChugInfo().getHealing());

            if (user.getTeam().isDead()) {
                drops.addAll(List.of(user.getTeam().getBackpack().getContents()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // run after everything else
    public void onPlayerDeath(PlayerDeathEvent event) {
        UHCUser user = userService.retrieve(event.getEntity()); // using retrieve because of combat logger
        if (!user.isPlaying()) return;

        UHCUser killer = user.getGameKiller(true);
        if (killer != null) {
            GameInfo killerGameInfo = killer.getGameInfo();

            killerGameInfo.getKills().increment();
            killerGameInfo.addKilledUser(user);
        }
        GameInfo gameInfo = user.getGameInfo();
        gameInfo.setDied(true);

        if (teamService.getAliveTeams().size() <= 5) {
            gameInfo.setDiedInTop5(true);
        }
        user.updateRespawnInfo();
        user.updateMatchInventory();
        user.setVelocity(new Vector(0, 1, 0));

        World world = user.getWorld();
        world.spawn(killer != null ? killer.getLocation() : user.getLocation(), ExperienceOrb.class)
                .setExperience(10 * 12); // spawn exp
        // world.strikeLightningEffect(user.getLocation()); nope

        if (user.isOffline()) {
            user.setState(UHCUserState.SPECTATOR); // if combat logger died, PlayerRespawnEvent won't fire
        }
        handleDeathMessage(event, killer);

        // Handle elo separately here to display the update message after death message
        int victimElo = user.getData().getElo().getTotal() + gameInfo.getEloGained().getValue();
        int killerElo = (killer != null ?
                killer.getData().getElo().getTotal() + killer.getGameInfo().getEloGained().getValue() :
                victimElo);
        int change = EloCalculator.getEloChange(killerElo, victimElo, EloCalculator.Result.WIN);

        gameInfo.getEloGained().add(-change);
        int newVictimElo = victimElo - change;

        if (killer != null) {
            GameInfo killerGameInfo = killer.getGameInfo();

            killerGameInfo.getEloGained().add(change);
            int newKillerElo = killerElo + change;

            user.sendMessage(CC.GOLD + "You " + CC.RED + "(" + newVictimElo + ") (-" + change + ")" +
                    CC.GOLD + " have been beaten by " +
                    CC.GREEN + killer.getDisplayName() + " (" + newKillerElo + ") (+" + change + ")" + CC.GOLD + ".");
            killer.sendMessage(CC.GOLD + "You " + CC.GREEN + "(" + newKillerElo + ") (+" + change + ")" +
                    CC.GOLD + " have beaten " +
                    CC.RED + user.getDisplayName() + " (" + newVictimElo + ") (-" + change + ")" + CC.GOLD + ".");
        } else {
            user.sendMessage(CC.GOLD + "You " + CC.RED + "(" + newVictimElo + ") (-" + change + ")" +
                    CC.GOLD + " have been beaten.");
        }
        gameService.checkIfGameShouldEnd();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        UHCUser user = userService.get(event.getPlayer());
        if (!user.isPlaying()) return;

        event.setRespawnLocation(user.getLocation());
        user.setState(UHCUserState.SPECTATOR);
        gameService.checkIfGameShouldEnd();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        UHCUser user = userService.retrieve(entity.getUniqueId()); // retrieve because of combat logger

        if (user.isScattering()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        boolean consumedItemIsGoldenHead = ItemUtil.isGoldenHead(item);

        if (consumedItemIsGoldenHead) {
            // For whatever reason you have to remove the effect that is given by vanilla first
            player.removePotionEffect(PotionEffectType.REGENERATION);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
        }

        // Store the amount of eaten golden apples 30 seconds before a player dies
        // This is here to prevent intentional chugging intended for the killer to not get any healing
        if (item.getType() == Material.GOLDEN_APPLE) {
            UHCUser user = userService.get(player);
            if (!user.isPlaying()) return;

            AntiChugInfo antiChugInfo = user.getAntiChugInfo();

            if (consumedItemIsGoldenHead) {
                antiChugInfo.updateGoldenHeads(true);
            } else {
                antiChugInfo.updateGoldenApples(true);
            }
            Executor.schedule(() -> {
                if (user.isPlaying()) {
                    if (consumedItemIsGoldenHead) {
                        antiChugInfo.updateGoldenHeads(false);
                    } else {
                        antiChugInfo.updateGoldenApples(false);
                    }
                }
            }).runSyncLater(30_000L);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        UHCUser user = userService.get(entity.getUniqueId());
        if (!user.isPlaying()) {
            event.setCancelled(true);
            return;
        }
        // Decrease hunger to 75%
        if (event.getFoodLevel() < user.getFoodLevel() && MathUtil.tryChance(75)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        UHCUser user = userService.get(event.getPlayer());

        if (!user.isPlaying()) {
            event.setCancelled(true);
            return;
        }
        if (block.getType() == Material.BOOKSHELF) {
            int dropCount = MathUtil.getIntBetween(1, 2);
            ItemStack itemInHand = event.getPlayer().getItemInHand();

            if (itemInHand != null
                    && itemInHand.hasItemMeta()
                    && itemInHand.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
                dropCount += itemInHand.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS);
            }
            block.setType(Material.AIR);
            ItemUtil.dropItem(new ItemStack(Material.BOOK, dropCount), block, event.getPlayer());
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (gameService.isNotState(GameState.PLAYING)) {
            event.setCancelled(true);
        }
        // Dont unload chunk if a CombatLogger is inside it because the entity will despawn
        Arrays.stream(event.getChunk().getEntities())
                .filter(entity -> entity instanceof Zombie && entity.hasMetadata("CombatLogger"))
                .findFirst()
                .ifPresent(entity -> event.setCancelled(true));
    }

    @EventHandler
    public void onGhastDeathEvent(EntityDeathEvent event) {
        // Don't drop ghast tears because we don't want to allow brewing of regeneration potions
        if (event.getEntity() instanceof Ghast) {
            List<ItemStack> drops = event.getDrops();
            drops.removeIf(drop -> drop.getType() == Material.GHAST_TEAR);
            drops.add(new ItemStack(Material.GOLD_INGOT));
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Material resultType = event.getRecipe().getResult().getType();

        // Melon is used to brew instant health/damage, we don't want that
        if (resultType == Material.SPECKLED_MELON) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnitityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        Entity victimEntity = event.getEntity();
        Entity damagerEntity = event.getDamager();

        // Handle Game Info
        if (victimEntity instanceof Player && damagerEntity instanceof Player) {
            Player playerDamager = (Player) damagerEntity;
            ItemStack item = playerDamager.getItemInHand();

            if (item != null && (item.getType().name().contains("SWORD") || item.getType().name().contains("AXE"))) {
                UHCUser user = userService.get(playerDamager);

                if (user.isPlaying()) {
                    user.getGameInfo().getLandedSwordHits().increment();
                    user.getGameInfo().getSwordHits().increment();
                }
            }
        }
        if (damagerEntity instanceof Arrow) {
            ProjectileSource source = ((Arrow) damagerEntity).getShooter();
            if (source instanceof Player) {
                UHCUser user = userService.get((Player) source);
                if (user.isPlaying()) {
                    user.getGameInfo().getLandedArrowShots().increment();
                }
            }
        }

        // Handle Bow Message
        if (!(victimEntity instanceof Player) || !(damagerEntity instanceof Arrow)) return;
        ProjectileSource projectileSource = ((Arrow) damagerEntity).getShooter();
        if (!(projectileSource instanceof Player)) return;

        UHCUser victim = userService.get(victimEntity.getUniqueId());
        double health = victim.getHealth() - event.getFinalDamage();
        if (health <= 0) return;

        UHCUser damager = userService.get((Player) projectileSource);
        double absorption = victim.getAbsorption() + event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
        double formattedHealth = Math.ceil(health) / 2;
        double formattedAbsorption = Math.ceil(absorption / 2);

        damager.sendMessage(CC.PURPLE + "[Health] " + victim.getColoredDisplayName() + CC.GRAY + " is now at " + CC.RED + formattedHealth + CC.DARK_RED + Lang.HEART
                + (absorption > 0f ? CC.GRAY + " (" + CC.YELLOW + formattedAbsorption + CC.GOLD + Lang.HEART + CC.GRAY + ')' : "") + CC.GRAY + '.');
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) return;

        if (borderService.isFirstShrinkOccurred()) {
            event.getPlayer().sendMessage(CC.RED + "You cannot go into the nether after the first border shrink.");
            event.setCancelled(true);
            return;
        }
        UHCUser user = userService.get(event.getPlayer());

        if (user.isRegularSpectator()) {
            event.getPlayer().sendMessage(CC.RED + "You cannot go to the nether.");
            event.setCancelled(true);
        }
        World fromWorld = event.getFrom().getWorld();
        Location location = user.getLocation();
        TravelAgent agent = event.getPortalTravelAgent();
        agent.setSearchRadius(32);

        if (fromWorld == worldService.getUhcWorld()) {
            double x = location.getX() / 8.0D;
            double y = location.getY();
            double z = location.getZ() / 8.0D;

            event.setTo(agent.findOrCreate(new Location(worldService.getNetherWorld(), x, y, z)));

            if (user.isPlaying()) {
                Clickable clickable = new Clickable()
                        .add(CC.RED + "[Warning] " + user.getColoredDisplayName() + CC.GRAY + " just entered the " + CC.PURPLE + "Nether" + CC.GRAY + ". ")
                        .add(CC.GREEN + "[Teleport]", CC.GREEN + "Click to teleport.", "/tp " + user.getDisplayName());
                userService.getModAndHostUsers()
                        .stream()
                        .filter(gameMod -> gameMod.getStaffData().isXrayAlerts())
                        .forEach(clickable::send);
                user.getGameInfo().getNethersEntered().increment();
            }
        } else if (fromWorld == worldService.getNetherWorld()) {
            double x = location.getX() * 8D;
            double y = location.getY();
            double z = location.getZ() * 8D;

            // Correction here because we can't teleport user outside the border
            if (x > 0) x = Math.min(x, borderService.getCurrentRadius().getValue() - 10);
            if (x < 0) x = Math.max(x, -borderService.getCurrentRadius().getValue() + 10);

            if (z > 0) z = Math.min(z, borderService.getCurrentRadius().getValue() - 10);
            if (z < 0) z = Math.max(z, -borderService.getCurrentRadius().getValue() + 10);

            event.setTo(agent.findOrCreate(new Location(worldService.getUhcWorld(), x, y, z)));
        }
    }

    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        UHCUser user = userService.get(event.getPlayer());

        int oldLevel = event.getOldLevel();
        int newLevel = event.getNewLevel();
        int difference = Math.abs(oldLevel - newLevel);

        if (newLevel > oldLevel && user.isPlaying()) {
            user.getGameInfo().getLevelsEarned().add(difference);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        UHCUser user = userService.get(event.getPlayer());
        ItemStack item = user.getItemInHand();
        Action action = event.getAction();

        if (item != null && user.isPlaying()) {
            if ((item.getType().name().contains("SWORD") || item.getType().name().contains("AXE"))
                    && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                user.getGameInfo().getSwordHits().increment();
            }
        }
        if (!user.isPlaying()
                && event.getAction() == Action.PHYSICAL
                && event.getClickedBlock().getType() == Material.SOIL)
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        UHCUser user = userService.get(event.getEntity().getUniqueId());

        if (user.isPlaying()) {
            user.getData().getArrowShots().increment();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGameStart(GameStartEvent event) {
        if (gameService.isMatchSynchronized()) return;

        Executor.schedule(() -> {
            UHCMatch match = matchService.createMatch(UHCMatch.class, callback -> callback.setStartTime(System.currentTimeMillis()));
            gameService.setMatch(match);
        }).runAsync();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameEnd(GameEndEvent event) {
        Team winningTeam = event.getWinningTeam();

        winningTeam.getMembers().forEach(member -> {
            member.getGameInfo().setWinner(true);

            if (!member.isPlaying()) {
                member.getGameInfo().setCarriedToVictory(true);
            }
        });
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (MathUtil.tryChance(NumberOption.APPLE_RATE.getValue())) {
            Block block = event.getBlock();
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.APPLE));
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        UHCUser user = userService.get(event.getPlayer());

        if (!user.isPlaying()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        UHCUser user = userService.get(event.getPlayer());

        if (!user.isPlaying()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        UHCUser user = userService.get(event.getPlayer());

        if (!user.isPlaying()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        UHCUser user = userService.get(event.getPlayer());

        if (!user.isPlaying()) {
            event.setCancelled(true);
        }
    }

    private void handleDeathMessage(PlayerDeathEvent event, UHCUser killer) {
        UHCUser victim = userService.retrieve(event.getEntity()); // using retrieve because of combat logger
        boolean hasKiller = killer != null;
        EntityDamageEvent lastDamageCause = victim.getLastDamageCause();
        String message;

        if (lastDamageCause == null) {
            message = "<victim> " + CC.GRAY + "died mysteriously.";
        } else switch (victim.getLastDamageCause().getCause()) {
            case PROJECTILE:
                if (hasKiller) {
                    int distance = (int) victim.getLocation().distance(killer.getLocation());
                    message = "<victim> " + CC.GRAY + "was shot by <killer> " + CC.GRAY + "from " + CC.BLUE + distance + CC.GRAY + " blocks.";
                } else {
                    message = "<victim> " + CC.GRAY + "was shot by an arrow.";
                }
                break;
            case CONTACT:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "walked into a cactus while trying to escape <killer>.";
                } else {
                    message = "<victim> " + CC.GRAY + "hugged a cactus.";
                }
                break;
            case DROWNING:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "drowned whilst trying to escape <killer>.";
                } else {
                    message = "<victim> " + CC.GRAY + "drowned.";
                }
                break;
            case SUFFOCATION:
                message = "<victim> " + CC.GRAY + "suffocated in a wall.";
                break;
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "was blown up by <killer>.";
                } else {
                    message = "<victim> " + CC.GRAY + "blew up.";
                }
                break;
            case FALL:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "was doomed to fall by <killer>.";
                } else {
                    message = "<victim> " + CC.GRAY + "hit the ground too hard.";
                }
                break;
            case FALLING_BLOCK:
                message = "<victim> " + CC.GRAY + "was squashed by a falling block.";
                break;
            case FIRE_TICK:
            case FIRE:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "was burnt to a crisp whilst fighting <killer>.";
                } else {
                    message = "<victim> " + CC.GRAY + "went up in flames.";
                }
                break;
            case LAVA:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "tried to swim in lava while trying to escape <killer>.";
                } else {
                    message = "<victim> " + CC.GRAY + "tried to swim in lava.";
                }
                break;
            case LIGHTNING:
                message = "<victim> " + CC.GRAY + " was struck by lightning.";
                break;
            case ENTITY_ATTACK:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "was slain by <killer>.";
                } else {
                    message = "<victim> " + CC.GRAY + "died mysteriously.";
                }
                break;
            case POISON:
            case MAGIC:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "was killed by <killer> " + CC.GRAY + "using magic";
                } else {
                    message = "<victim> " + CC.GRAY + "was killed by magic.";
                }
                break;
            case STARVATION:
                message = "<victim> " + CC.GRAY + "starved to death.";
                break;
            case THORNS:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "was killed while trying to hurt <killer>.";
                } else {
                    message = "<victim> " + CC.GRAY + "was killed.";
                }
                break;
            case VOID:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "didn't want to live in the same world as <killer>.";
                } else {
                    message = "<victim> " + CC.GRAY + "fell out of the world.";
                }
                break;
            case WITHER:
                message = "<victim> " + CC.GRAY + "withered away.";
                break;
            case SUICIDE:
                message = "<victim> " + CC.GRAY + "killed themselves.";
                break;
            case MELTING:
                message = "<victim> " + CC.GRAY + "melted to death.";
                break;
            case CUSTOM:
                message = victim.getDeathMessage();
                break;
            default:
                if (hasKiller) {
                    message = "<victim> " + CC.GRAY + "was killed by <killer>.";
                } else {
                    message = "<victim> " + CC.GRAY + "died mysteriously.";
                }
                break;
        }
        event.setDeathMessage(null); // don't let spigot send the default message

        String victimFormat = victim.getColoredDisplayName() + CC.YELLOW + '[' + victim.getGameInfo().getKills().getValue() + ']';
        String killerFormat = "";

        if (hasKiller) {
            killerFormat = killer.getColoredDisplayName() + CC.YELLOW + '[' + killer.getGameInfo().getKills().getValue() + ']';
        }
        String finalMessage = message.replace("<victim>", victimFormat).replace("<killer>", killerFormat);

        userService.getOnlineUsers()
                .stream()
                .filter(user -> user.getData().isDeathMessages())
                .forEach(user -> user.sendMessage(finalMessage));
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(false);
            return;
        }
        LivingEntity entity = event.getEntity();
        if (LocationUtil.isOutsideRadius(event.getLocation(), borderService.getCurrentRadius().getValue()) ||
                borderService.getCurrentRadius().isEqualOrLower(BorderRadius.RADIUS_100)) {
            event.setCancelled(true);
            return;
        }
        if (entity.getType() == EntityType.BAT ||
                entity.getType() == EntityType.SQUID ||
                entity.getType() == EntityType.GHAST ||
                entity.getType() == EntityType.RABBIT ||
                entity.getType() == EntityType.PIG) {
            event.setCancelled(true);
            return;
        }
        if (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.SKELETON ||
                entity.getType() == EntityType.CREEPER || entity.getType() == EntityType.SPIDER ||
                entity.getType() == EntityType.ENDERMAN || entity.getType() == EntityType.WITCH) {
            if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Stream.of(Material.SAPLING, Material.SEEDS)
                .filter(material -> event.getEntity().getItemStack().getType() == material)
                .forEach(material -> event.setCancelled(true));
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.getEntity() instanceof CombatLoggerEntity) {
            event.setCancelled(true);
        }
    }
}
