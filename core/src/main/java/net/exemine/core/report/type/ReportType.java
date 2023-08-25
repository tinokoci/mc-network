package net.exemine.core.report.type;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.exemine.api.instance.InstanceType;
import org.bukkit.Material;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ReportType {

    CHEATING("Cheating", Material.DIAMOND_SWORD, null),
    CAMPING("Camping", Material.BOAT, null),
    XRAY("X-Ray", Material.DIAMOND_ORE, null),
    ALLYING("Allying", Material.GOLDEN_APPLE, null),
    KITING("Kiting", Material.GOLD_BOOTS, null),
    CHAT("Chat", Material.PAPER, null),
    OTHER("Other", Material.ANVIL, null);

    private final String name;
    private final Material material;
    private final InstanceType type;

    public static int getSize(InstanceType type) {
        return (int) ((Arrays.stream(values()).filter(report -> report.getType() == null || report.getType() == type).count() / 9) + 1);
    }
}
