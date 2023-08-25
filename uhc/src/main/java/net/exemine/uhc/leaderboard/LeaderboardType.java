package net.exemine.uhc.leaderboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.util.List;

@RequiredArgsConstructor
@Getter
public enum LeaderboardType {

    COMBAT("Combat", Material.DIAMOND_SWORD,
            List.of("elo", "wins", "kills", "deaths", "kdr"),
            List.of(Material.NETHER_STAR, Material.FIREWORK, Material.IRON_SWORD, Material.SKULL_ITEM, Material.BOW)),
    MINING("Mining",
            Material.DIAMOND_PICKAXE, List.of("mined-diamonds", "mined-gold", "mined-iron", "mined-redstone", "mined-lapis", "mined-coal", "mined-quartz"),
            List.of(Material.DIAMOND_ORE, Material.GOLD_ORE, Material.IRON_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE, Material.COAL_ORE, Material.QUARTZ_ORE)),
    OTHER("Other",
            Material.ENCHANTMENT_TABLE, List.of("games-played", "top5s", "carried-wins", "levels-earned", "nethers-entered"),
            List.of(Material.WATCH, Material.GOLD_AXE, Material.FEATHER, Material.EXP_BOTTLE, Material.NETHER_BRICK));

    private final String name;
    private final Material material;
    private final List<String> keys;
    private final List<Material> materials;
}
