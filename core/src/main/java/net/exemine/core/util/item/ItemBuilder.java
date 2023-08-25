package net.exemine.core.util.item;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.exemine.api.util.callable.TypeCallback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@NoArgsConstructor
@Setter
@Accessors(chain = true)
public class ItemBuilder {

    private ItemStack item;
    private Material material = Material.STONE;
    private int amount = 1;
    private int durability = -1;
    private String name;
    private List<String> lore = new ArrayList<>();
    private Enchantment enchantment;
    private int enchantmentLevel = 1;
    private boolean unbreakable;
    private Color color;
    private boolean cloned;

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.cloned = true;
    }

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        this.lore = List.of(lore);
        return this;
    }

    public ItemBuilder setLore(TypeCallback<List<String>> loreCallback) {
        List<String> lore = new ArrayList<>();
        loreCallback.run(lore);
        this.lore = lore;
        return this;
    }

    public ItemBuilder setLore(Supplier<List<String>> loreSupplier) {
        this.lore = loreSupplier.get();
        return this;
    }

    public ItemBuilder addLore(String line) {
        if (line != null) {
            lore.add(line);
        }
        return this;
    }

    public ItemBuilder addLore(List<String> lines) {
        lore.addAll(lines);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        if (enchantment == null) return this;

        this.enchantment = enchantment;
        this.enchantmentLevel = level;
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment) {
        return addEnchantment(enchantment, 1);
    }

    public ItemBuilder unbreakable() {
        unbreakable = true;
        return this;
    }

    public ItemStack build() {
        if (!cloned) {
            item = new ItemStack(material, Math.min(amount, 64));
        }
        if (durability != -1) {
            item.setDurability((short) durability);
        }
        ItemMeta meta = item.getItemMeta();

        if (name != null) {
            meta.setDisplayName(name);
        }
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }
        if (unbreakable) {
            meta.spigot().setUnbreakable(true);
        }
        if (material.name().startsWith("LEATHER_") && color != null) {
            ((LeatherArmorMeta) meta).setColor(color);
        }
        item.setItemMeta(meta);
        if (enchantment != null) {
            item.addUnsafeEnchantment(enchantment, enchantmentLevel);
        }
        return item;
    }

    public static ItemBuilder getPlayerHead(Player owner) {
        return getPlayerHead(owner.getName());
    }

    public static ItemBuilder getPlayerHead(String owner) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1);
        item.setDurability((short) 3);

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(owner);

        item.setItemMeta(meta);

        return new ItemBuilder(item);
    }

    public static ItemStack getGoldenHead(int amount) {
        return new ItemBuilder()
                .setMaterial(Material.GOLDEN_APPLE)
                .setAmount(amount)
                .setName(ItemUtil.GOLDEN_HEAD_NAME)
                .build();
    }

    public static ItemStack getGoldenHead() {
        return getGoldenHead(1);
    }

    public static void addGoldenHeadRecipe() {
        ShapedRecipe goldenHead = new ShapedRecipe(ItemBuilder.getGoldenHead());
        goldenHead.shape("GGG", "GHG", "GGG");
        goldenHead.setIngredient('G', Material.GOLD_INGOT);
        goldenHead.setIngredient('H', Material.SKULL_ITEM, 3);
        Bukkit.getServer().addRecipe(goldenHead);
    }

    public static void addStringFromWoolRecipe() {
        ShapelessRecipe string = new ShapelessRecipe(new ItemStack(Material.STRING));
        string.addIngredient(4, Material.WOOL);
        Bukkit.getServer().addRecipe(string);
    }

    public static void removeGoldenHeadRecipe() {
        Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();

        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();

            if (recipe.getResult().equals(getGoldenHead())) {
                iterator.remove();
            }
        }
    }

    public static List<String> wrapLore(String input, int length) {
        List<String> result = new ArrayList<>();

        // We need this to ensure the color code is applied to all lines
        String colorCode = ChatColor.getLastColors(input);

        // Split the input string into words
        String[] words = input.split("\\s+");

        // Initialize a counter to keep track of the current line length
        int lineLength = 0;

        // Initialize a string builder to hold the current line
        StringBuilder builder = new StringBuilder();

        // Iterate through each word in the input string
        for (String word : words) {
            // Check if the current word fits on the current line
            if (lineLength + word.length() > length) {
                // If it doesn't fit, add the current line to the result list
                result.add(colorCode + builder.toString());

                // Reset the string builder and the counter
                builder = new StringBuilder();
                lineLength = 0;
            }

            // Append the word to the current line and update the counter
            builder.append(word).append(" ");
            lineLength += word.length() + 1;
        }

        // Add the last line to the result list
        result.add(colorCode + builder);

        // Return the result
        return result;
    }
}