package net.exemine.uhc.user.info;

import net.exemine.api.data.stat.number.impl.SimpleIntStat;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AntiChugInfo {

    private final SimpleIntStat goldenApples = new SimpleIntStat();
    private final SimpleIntStat goldenHeads = new SimpleIntStat();

    public void updateGoldenApples(boolean add) {
        if (add) {
            goldenApples.increment();
        } else {
            goldenApples.decrement();
        }
    }

    public void updateGoldenHeads(boolean add) {
        if (add) {
            goldenHeads.increment();
        } else {
            goldenHeads.decrement();
        }
    }

    public List<ItemStack> getHealing() {
        return new ArrayList<ItemStack>() {{
            if (goldenApples.getValue() > 0) add(new ItemStack(Material.GOLDEN_APPLE, goldenApples.getValue()));
            if (goldenHeads.getValue() > 0) add(ItemBuilder.getGoldenHead(goldenHeads.getValue()));
        }};
    }

    public void clear() {
        goldenApples.setValue(0);
        goldenHeads.setValue(0);
    }
}
