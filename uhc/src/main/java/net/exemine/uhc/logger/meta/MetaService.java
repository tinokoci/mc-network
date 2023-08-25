package net.exemine.uhc.logger.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.exemine.api.util.MathUtil;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MetaService {

    private final Map<Material, MaterialXpInfo> xpFromOresMap = new HashMap<Material, MaterialXpInfo>() {{
        put(Material.COAL_ORE, new MaterialXpInfo(0, 1));
        put(Material.IRON_ORE, new MaterialXpInfo(1));
        put(Material.GOLD_ORE, new MaterialXpInfo(2));
        put(Material.DIAMOND_ORE, new MaterialXpInfo(3));
        put(Material.EMERALD_ORE, new MaterialXpInfo(3));
        put(Material.LAPIS_ORE, new MaterialXpInfo(3));
        put(Material.REDSTONE_ORE, new MaterialXpInfo(1));
        put(Material.GLOWING_REDSTONE_ORE, new MaterialXpInfo(1));
        put(Material.QUARTZ_ORE, new MaterialXpInfo(1));
    }};

    public int getXpFromOre(Material material) {
        MaterialXpInfo info = xpFromOresMap.get(material);
        if (info == null) return 0;
        return info.getRandomValue();
    }

    public boolean updateXpForOre(Material material, int minInclusive, int maxInclusive) {
        MaterialXpInfo info = xpFromOresMap.get(material);
        if (info == null) return false;
        info.setMinInclusive(minInclusive);
        info.setMaxInclusive(maxInclusive);
        return true;
    }

    public Set<Map.Entry<Material, MaterialXpInfo>> getAllXpFromOresInfo() {
        return xpFromOresMap.entrySet();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class MaterialXpInfo {

        private int minInclusive;
        private int maxInclusive;

        public MaterialXpInfo(int value) {
            minInclusive = value;
            maxInclusive = value;
        }

        public int getRandomValue() {
            return MathUtil.getIntBetween(minInclusive, maxInclusive);
        }
    }
}
