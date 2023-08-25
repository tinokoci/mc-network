package net.exemine.core.cosmetic.bow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.exemine.api.cosmetic.Cosmetic;
import net.exemine.api.cosmetic.CosmeticType;
import net.exemine.core.util.particle.ParticleEffect;
import org.bukkit.Material;

import javax.annotation.Nullable;
import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum BowTrail implements Cosmetic {

    WATER_SPLASH("Water Splash", ParticleEffect.WATER_SPLASH, Material.WATER_BUCKET),
    ANGRY_VILLAGER("Angry Villager", ParticleEffect.VILLAGER_ANGRY, Material.GLOWSTONE_DUST),
    HAPPY_VILLAGER("Happy Villager", ParticleEffect.VILLAGER_HAPPY, Material.EMERALD),
    HEART("Heart", ParticleEffect.HEART, Material.DOUBLE_PLANT),
    FLAME("Flame", ParticleEffect.FLAME, Material.FIREWORK_CHARGE),
    SNOWBALL("Snowball", ParticleEffect.SNOWBALL, Material.SNOW_BALL),
    REDSTONE("Redstone", ParticleEffect.REDSTONE, Material.REDSTONE),
    SLIME("Slime", ParticleEffect.SLIME, Material.SLIME_BALL),
    SMOKE("Smoke", ParticleEffect.SMOKE_NORMAL, Material.FLINT_AND_STEEL),
    SPARK("Spark", ParticleEffect.FIREWORKS_SPARK, Material.FIREWORK),
    CLOUD("Cloud", ParticleEffect.CLOUD, Material.WEB),
    ENCHANT("Enchant", ParticleEffect.ENCHANTMENT_TABLE, Material.ENCHANTMENT_TABLE),
    SPELL("Spell", ParticleEffect.SPELL_WITCH, Material.POTION),
    NOTE("Note", ParticleEffect.NOTE, Material.NOTE_BLOCK);

    private final String name;
    private final ParticleEffect effect;
    private final Material displayItem;

    public CosmeticType getType() {
        return CosmeticType.BOW_TRAIL;
    }

    @Nullable
    public static BowTrail get(String name) {
        return Arrays.stream(BowTrail.values())
                .filter(prefix -> prefix.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}