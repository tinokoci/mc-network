package net.exemine.uhc.practice;

import com.execets.spigot.ExeSpigot;
import net.exemine.api.data.stat.number.impl.SimpleIntStat;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.util.ServerUtil;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.particle.ParticleEffect;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.location.LocationService;
import net.exemine.uhc.practice.event.PracticeKillStreakEvent;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;
import net.exemine.uhc.user.info.GameInfo;
import net.exemine.uhc.world.WorldService;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;

public class PracticeListener extends ConfigListener {

    private boolean initialized;

    private LocationService locationService;
    private UHCUserService userService;
    private WorldService worldService;

    private PracticeMoveInterceptor moveInterceptor;

    @Override
    public void onEnable() {
        if (!initialized) {
            locationService = plugin.getLocationService();
            userService = plugin.getUserService();
            worldService = plugin.getWorldService();
            moveInterceptor = new PracticeMoveInterceptor(locationService, userService);
            initialized = true;
        }
        ExeSpigot.INSTANCE.addMovementHandler(moveInterceptor);
    }

    @Override
    public void onDisable() {
        ExeSpigot.INSTANCE.addMovementHandler(moveInterceptor);
        userService.getPracticeUsers().forEach(practiceUser -> practiceUser.setState(UHCUserState.LOBBY));
    }

    @EventHandler
    public void onPracticeKillStreak(PracticeKillStreakEvent event) {
        UHCUser user = event.getUser();
        int value = event.getValue();

        ServerUtil.launchFirework(user.getLocation(), Color.ORANGE, 2, true, true);
        String message = CC.BOLD_GOLD + "STREAK! " + user.getColoredDisplayName() + CC.GRAY + " has killed " + CC.PINK + value + CC.GRAY + " players in a row.";
        userService.getPracticeUsers().forEach(practiceUser -> practiceUser.sendMessage(message));

        PlayerInventory inventory = user.getInventory();
        // Could maybe be switched with an enum or something, but since rewards aren't
        // the same this approach is alright in my opinion
        switch (value) {
            case 5:
                Arrays.stream(inventory.getContents())
                        .filter(item -> item.getType() == Material.DIAMOND_SWORD)
                        .findFirst()
                        .ifPresent(item -> {
                            item.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                            user.sendMessage(CC.PURPLE + "[Streak] " + CC.GRAY + "Your sword got applied with " + CC.GOLD + "Sharpness 1 " + CC.GRAY + "as a reward.");
                        });
                break;
            case 10:
                ItemStack leggings = inventory.getLeggings();
                if (leggings == null) return;

                leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                user.sendMessage(CC.PURPLE + "[Streak] " + CC.GRAY + "Your leggings got applied with " + CC.GOLD + "Protection 1 " + CC.GRAY + "as a reward.");
                break;
            case 15:
            default:
                inventory.addItem(ItemBuilder.getGoldenHead(3));
                user.sendMessage(CC.PURPLE + "[Streak] " + CC.GRAY + "You received " + CC.GOLD + "3x Golden Head " + CC.GRAY + "as a reward.");
                break;
            case 20:
                ItemStack helmet = inventory.getHelmet();
                if (helmet == null) return;

                helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                user.sendMessage(CC.PURPLE + "[Streak] " + CC.GRAY + "Your helmet got applied with " + CC.GOLD + "Protection 1 " + CC.GRAY + "as a reward.");
                break;
            case 25:
                Arrays.stream(inventory.getContents())
                        .filter(item -> item.getType() == Material.DIAMOND_SWORD)
                        .findFirst()
                        .ifPresent(item -> {
                            item.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                            user.sendMessage(CC.PURPLE + "[Streak] " + CC.GRAY + "Your sword got applied with " + CC.GOLD + "Sharpness 2 " + CC.GRAY + "as a reward.");
                        });
        }
        user.updateInventory();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UHCUser user = userService.retrieve(event.getEntity()); // using retrieve because of combat logger
        if (!user.isInPractice()) return;

        event.setDeathMessage(null);
        event.setDroppedExp(0);
        event.getDrops().clear();

        Player playerKiller = event.getEntity().getKiller();
        if (playerKiller == null) return;

        UHCUser killer = userService.get(playerKiller);
        if (user == killer) return;

        GameInfo killerStats = killer.getGameInfo();
        killerStats.getPracticeKills().increment();
        SimpleIntStat streak = killerStats.getPracticeStreak();
        streak.increment();

        GameInfo victimStats = user.getGameInfo();
        victimStats.getPracticeDeaths().increment();
        victimStats.getPracticeStreak().setValue(0);

        String heartsFormat = CC.RED + killer.getFormattedHealth() + CC.DARK_RED + Lang.HEART;
        String absorptionFormat = killer.getAbsorption() == 0f
                ? ""
                : ' ' + CC.GRAY + '(' + CC.YELLOW + killer.getFormattedAbsorption() + CC.GOLD + Lang.HEART + CC.GRAY + ')';

        user.sendMessage(CC.PURPLE + "[Practice] " + CC.GRAY + "You got killed by " + killer.getColoredDisplayName() + CC.GRAY + " with " + heartsFormat + absorptionFormat + CC.GRAY + '.');
        killer.sendMessage(CC.PURPLE + "[Practice] " + CC.GRAY + "You have killed " + user.getColoredDisplayName() + CC.GRAY + " with " + heartsFormat + absorptionFormat + CC.GRAY + '.');

        if (streak.getValue() % 5 == 0 && streak.getValue() > 0) {
            Bukkit.getPluginManager().callEvent(new PracticeKillStreakEvent(killer, streak.getValue()));
        }
        killer.getInventory().addItem(ItemBuilder.getGoldenHead(1));
        killer.playSound(Sound.ITEM_PICKUP);
        user.playSound(Sound.ENDERDRAGON_HIT);

        ParticleEffect.SMOKE_LARGE.display(0, 0, 0, 0, 1, user.getLocation().clone().add(0, 1, 0), 256);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        UHCUser user = userService.get(event.getPlayer());

        if (user.isInPractice() && user.isOnline()) {
            event.setRespawnLocation(locationService.getPracticeScatterLocation());
            user.setState(UHCUserState.PRACTICE);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        UHCUser user = userService.get(event.getEntity().getUniqueId());

        if (user.isInPractice()) {
            event.setCancelled(true);

            if (user.getFoodLevel() < 20) {
                user.setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        UHCUser user = userService.retrieve((Player) event.getEntity()); // retrieve because of combat logger

        if (user.isInPractice() && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        UHCUser user = userService.get(event.getWhoClicked().getUniqueId());

        if (user.isInPractice() && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        World world = event.getEntity().getWorld();

        if (worldService.isWorld(world, worldService.getPracticeWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        World world = event.getEntity().getWorld();

        if (worldService.isWorld(world, worldService.getPracticeWorld())) {
            event.setCancelled(true);
        }
    }
}
